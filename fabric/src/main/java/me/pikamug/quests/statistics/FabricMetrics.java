/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.statistics;

import me.pikamug.quests.FabricQuestsPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class FabricMetrics {

  private final FabricQuestsPlugin plugin;

  private final MetricsBase metricsBase;

  private final Properties properties = new Properties();

  private File configFile;

  /**
   * Creates a new Metrics instance.
   *
   * @param plugin Your plugin instance.
   */
  public FabricMetrics(final FabricQuestsPlugin plugin) {
    this.plugin = plugin;
    final int serviceId = 9528;
    // Get the config file
    final File bStatsFolder = new File(plugin.getPluginDataFolder(), "bStats");
    configFile = new File(bStatsFolder, "config.properties");
    if (!configFile.exists()) {
      if (!bStatsFolder.exists() && !bStatsFolder.mkdirs()) {
        // Accept metric settings file creation failure and continue
      }
      properties.setProperty("enabled", "true");
      properties.setProperty("serverUuid", UUID.randomUUID().toString());
      properties.setProperty("logFailedRequests", "false");
      properties.setProperty("logSentData", "false");
      properties.setProperty("logResponseStatusText", "false");
      saveConfig();
    } else {
      try (final InputStream in = new FileInputStream(configFile)) {
        properties.load(in);
      } catch (final IOException ignored) {
      }
      if (!properties.containsKey("serverUuid")) {
        properties.setProperty("serverUuid", UUID.randomUUID().toString());
        saveConfig();
      }
    }
    // Load the data
    final boolean enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
    final String serverUUID = properties.getProperty("serverUuid");
    final boolean logErrors = Boolean.parseBoolean(properties.getProperty("logFailedRequests", "false"));
    final boolean logSentData = Boolean.parseBoolean(properties.getProperty("logSentData", "false"));
    final boolean logResponseStatusText = Boolean.parseBoolean(properties.getProperty("logResponseStatusText", "false"));
    metricsBase =
        new MetricsBase(
            "fabric",
            serverUUID,
            serviceId,
            enabled,
            this::appendPlatformData,
            this::appendServiceData,
            submitDataTask -> plugin.getServer().execute(submitDataTask::run),
            plugin::isEnabled,
            (message, error) -> plugin.getPluginLogger().warn(message, error),
            (message) -> plugin.getPluginLogger().info(message),
            logErrors,
            logSentData,
            logResponseStatusText);
  }

  private void saveConfig() {
    try (final OutputStream out = new FileOutputStream(configFile)) {
      properties.store(out, String.join(System.lineSeparator(),
          "bStats (https://bStats.org) collects some basic information for plugin authors, like how",
          "many people use their plugin and their total player count. It's recommended to keep bStats",
          "enabled, but if you're not comfortable with this, you can turn this setting off. There is no",
          "performance penalty associated with having metrics enabled, and data sent to bStats is fully",
          "anonymous."));
    } catch (final IOException ignored) {
    }
  }

  /**
   * Adds a custom chart.
   *
   * @param chart The chart to add.
   */
  public void addCustomChart(final CustomChart chart) {
    metricsBase.addCustomChart(chart);
  }

  private void appendPlatformData(final JsonObjectBuilder builder) {
    builder.appendField("playerAmount", getPlayerAmount());
    builder.appendField("onlineMode", getOnlineMode() ? 1 : 0);
    builder.appendField("fabricVersion", SharedConstants.getCurrentVersion().getName());
    builder.appendField("minecraftVersion", SharedConstants.getCurrentVersion().getName());
    builder.appendField("javaVersion", System.getProperty("java.version"));
    builder.appendField("osName", System.getProperty("os.name"));
    builder.appendField("osArch", System.getProperty("os.arch"));
    builder.appendField("osVersion", System.getProperty("os.version"));
    builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
  }

  private void appendServiceData(final JsonObjectBuilder builder) {
    final String pluginVersion = FabricLoader.getInstance().getModContainer(FabricQuestsPlugin.MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("5.3.3");
    final String adjustedVersion = pluginVersion.contains("-")
            ? pluginVersion.substring(0, pluginVersion.indexOf("-")) : pluginVersion;
    builder.appendField("pluginVersion", adjustedVersion);
  }

  private int getPlayerAmount() {
    final MinecraftServer server = plugin.getServer();
    return server == null ? 0 : server.getPlayerList().getPlayers().size();
  }

  private boolean getOnlineMode() {
    final MinecraftServer server = plugin.getServer();
    return server != null && server.usesAuthentication();
  }

  public static class MetricsBase {

    /** The version of the Metrics class. */
    public static final String METRICS_VERSION = "2.2.1";

    private static final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1, task -> new Thread(task, "bStats-Metrics"));

    private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";

    private final String platform;

    private final String serverUuid;

    private final int serviceId;

    private final Consumer<JsonObjectBuilder> appendPlatformDataConsumer;

    private final Consumer<JsonObjectBuilder> appendServiceDataConsumer;

    private final Consumer<Runnable> submitTaskConsumer;

    private final Supplier<Boolean> checkServiceEnabledSupplier;

    private final BiConsumer<String, Throwable> errorLogger;

    private final Consumer<String> infoLogger;

    private final boolean logErrors;

    private final boolean logSentData;

    private final boolean logResponseStatusText;

    private final Set<CustomChart> customCharts = new HashSet<>();

    private final boolean enabled;

    /**
     * Creates a new MetricsBase class instance.
     *
     * @param platform The platform of the service.
     * @param serviceId The id of the service.
     * @param serverUuid The server uuid.
     * @param enabled Whether or not data sending is enabled.
     * @param appendPlatformDataConsumer A consumer that receives a {@code JsonObjectBuilder} and
     *     appends all platform-specific data.
     * @param appendServiceDataConsumer A consumer that receives a {@code JsonObjectBuilder} and
     *     appends all service-specific data.
     * @param submitTaskConsumer A consumer that takes a runnable with the submit task. This can be
     *     used to delegate the data collection to a another thread to prevent errors caused by
     *     concurrency. Can be {@code null}.
     * @param checkServiceEnabledSupplier A supplier to check if the service is still enabled.
     * @param errorLogger A consumer that accepts log message and an error.
     * @param infoLogger A consumer that accepts info log messages.
     * @param logErrors Whether or not errors should be logged.
     * @param logSentData Whether or not the sent data should be logged.
     * @param logResponseStatusText Whether or not the response status text should be logged.
     */
    public MetricsBase(
        final String platform,
        final String serverUuid,
        final int serviceId,
        final boolean enabled,
        final Consumer<JsonObjectBuilder> appendPlatformDataConsumer,
        final Consumer<JsonObjectBuilder> appendServiceDataConsumer,
        final Consumer<Runnable> submitTaskConsumer,
        final Supplier<Boolean> checkServiceEnabledSupplier,
        final BiConsumer<String, Throwable> errorLogger,
        final Consumer<String> infoLogger,
        final boolean logErrors,
        final boolean logSentData,
        final boolean logResponseStatusText) {
      this.platform = platform;
      this.serverUuid = serverUuid;
      this.serviceId = serviceId;
      this.enabled = enabled;
      this.appendPlatformDataConsumer = appendPlatformDataConsumer;
      this.appendServiceDataConsumer = appendServiceDataConsumer;
      this.submitTaskConsumer = submitTaskConsumer;
      this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
      this.errorLogger = errorLogger;
      this.infoLogger = infoLogger;
      this.logErrors = logErrors;
      this.logSentData = logSentData;
      this.logResponseStatusText = logResponseStatusText;
      checkRelocation();
      if (enabled) {
        startSubmitting();
      }
    }

    public void addCustomChart(final CustomChart chart) {
      this.customCharts.add(chart);
    }

    private void startSubmitting() {
      final Runnable submitTask =
          () -> {
            if (!enabled || !checkServiceEnabledSupplier.get()) {
              // Submitting data or service is disabled
              scheduler.shutdown();
              return;
            }
            if (submitTaskConsumer != null) {
              submitTaskConsumer.accept(this::submitData);
            } else {
              this.submitData();
            }
          };
      // Many servers tend to restart at a fixed time at xx:00 which causes an uneven distribution
      // of requests on the
      // bStats backend. To circumvent this problem, we introduce some randomness into the initial
      // and second delay.
      // WARNING: You must not modify and part of this Metrics class, including the submit delay or
      // frequency!
      // WARNING: Modifying this code will get your plugin banned on bStats. Just don't do it!
      final long initialDelay = (long) (1000 * 60 * (3 + Math.random() * 3));
      final long secondDelay = (long) (1000 * 60 * (Math.random() * 30));
      scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
      scheduler.scheduleAtFixedRate(
          submitTask, initialDelay + secondDelay, 1000 * 60 * 30, TimeUnit.MILLISECONDS);
    }

    private void submitData() {
      final JsonObjectBuilder baseJsonBuilder = new JsonObjectBuilder();
      appendPlatformDataConsumer.accept(baseJsonBuilder);
      final JsonObjectBuilder serviceJsonBuilder = new JsonObjectBuilder();
      appendServiceDataConsumer.accept(serviceJsonBuilder);
      final JsonObjectBuilder.JsonObject[] chartData =
          customCharts.stream()
              .map(customChart -> customChart.getRequestJsonObject(errorLogger, logErrors))
              .filter(Objects::nonNull)
              .toArray(JsonObjectBuilder.JsonObject[]::new);
      serviceJsonBuilder.appendField("id", serviceId);
      serviceJsonBuilder.appendField("customCharts", chartData);
      baseJsonBuilder.appendField("service", serviceJsonBuilder.build());
      baseJsonBuilder.appendField("serverUUID", serverUuid);
      baseJsonBuilder.appendField("metricsVersion", METRICS_VERSION);
      final JsonObjectBuilder.JsonObject data = baseJsonBuilder.build();
      scheduler.execute(
          () -> {
            try {
              // Send the data
              sendData(data);
            } catch (final Exception e) {
              // Something went wrong! :(
              if (logErrors) {
                errorLogger.accept("Could not submit bStats metrics data", e);
              }
            }
          });
    }

    private void sendData(final JsonObjectBuilder.JsonObject data) throws Exception {
      if (logSentData) {
        infoLogger.accept("Sent bStats metrics data: " + data.toString());
      }
      final String url = String.format(REPORT_URL, platform);
      final HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
      // Compress the data to save bandwidth
      final byte[] compressedData = compress(data.toString());
      connection.setRequestMethod("POST");
      connection.addRequestProperty("Accept", "application/json");
      connection.addRequestProperty("Connection", "close");
      connection.addRequestProperty("Content-Encoding", "gzip");
      connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("User-Agent", "Metrics-Service/1");
      connection.setDoOutput(true);
      try (final DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
        outputStream.write(compressedData);
      }
      final StringBuilder builder = new StringBuilder();
      try (final BufferedReader bufferedReader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          builder.append(line);
        }
      }
      if (logResponseStatusText) {
        infoLogger.accept("Sent data to bStats and received response: " + builder);
      }
    }

    /** Checks that the class was properly relocated. */
    private void checkRelocation() {
      // You can use the property to disable the check in your test environment
      if (System.getProperty("bstats.relocatecheck") == null
          || !System.getProperty("bstats.relocatecheck").equals("false")) {
        // Maven's Relocate is clever and changes strings, too. So we have to use this little
        // "trick" ... :D
        final String defaultPackage =
            new String(new byte[] {'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's'});
        final String examplePackage =
            new String(new byte[] {'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
        // We want to make sure no one just copy & pastes the example and uses the wrong package
        // names
        if (MetricsBase.class.getPackage().getName().startsWith(defaultPackage)
            || MetricsBase.class.getPackage().getName().startsWith(examplePackage)) {
          throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
        }
      }
    }

    /**
     * Gzips the given string.
     *
     * @param str The string to gzip.
     * @return The gzipped string.
     */
    private static byte[] compress(final String str) throws IOException {
      if (str == null) {
        return null;
      }
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      try (final GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
        gzip.write(str.getBytes(StandardCharsets.UTF_8));
      }
      return outputStream.toByteArray();
    }
  }

  public static class AdvancedBarChart extends CustomChart {

    private final Callable<Map<String, int[]>> callable;

    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public AdvancedBarChart(final String chartId, final Callable<Map<String, int[]>> callable) {
      super(chartId);
      this.callable = callable;
    }

    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      final JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      final Map<String, int[]> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      boolean allSkipped = true;
      for (final Map.Entry<String, int[]> entry : map.entrySet()) {
        if (entry.getValue().length == 0) {
          // Skip this invalid
          continue;
        }
        allSkipped = false;
        valuesBuilder.appendField(entry.getKey(), entry.getValue());
      }
      if (allSkipped) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

  public static class SimpleBarChart extends CustomChart {

    private final Callable<Map<String, Integer>> callable;

    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public SimpleBarChart(final String chartId, final Callable<Map<String, Integer>> callable) {
      super(chartId);
      this.callable = callable;
    }

    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      final JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      final Map<String, Integer> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      for (final Map.Entry<String, Integer> entry : map.entrySet()) {
        valuesBuilder.appendField(entry.getKey(), new int[] {entry.getValue()});
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

  public static class MultiLineChart extends CustomChart {

    private final Callable<Map<String, Integer>> callable;

    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public MultiLineChart(final String chartId, final Callable<Map<String, Integer>> callable) {
      super(chartId);
      this.callable = callable;
    }

    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      final JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      final Map<String, Integer> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      boolean allSkipped = true;
      for (final Map.Entry<String, Integer> entry : map.entrySet()) {
        if (entry.getValue() == 0) {
          // Skip this invalid
          continue;
        }
        allSkipped = false;
        valuesBuilder.appendField(entry.getKey(), entry.getValue());
      }
      if (allSkipped) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

  public static class AdvancedPie extends CustomChart {

    private final Callable<Map<String, Integer>> callable;

    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public AdvancedPie(final String chartId, final Callable<Map<String, Integer>> callable) {
      super(chartId);
      this.callable = callable;
    }

    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      final JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      final Map<String, Integer> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      boolean allSkipped = true;
      for (final Map.Entry<String, Integer> entry : map.entrySet()) {
        if (entry.getValue() == 0) {
          // Skip this invalid
          continue;
        }
        allSkipped = false;
        valuesBuilder.appendField(entry.getKey(), entry.getValue());
      }
      if (allSkipped) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

  public abstract static class CustomChart {

    private final String chartId;

    protected CustomChart(final String chartId) {
      if (chartId == null) {
        throw new IllegalArgumentException("chartId must not be null");
      }
      this.chartId = chartId;
    }

    public JsonObjectBuilder.JsonObject getRequestJsonObject(
        final BiConsumer<String, Throwable> errorLogger, final boolean logErrors) {
      final JsonObjectBuilder builder = new JsonObjectBuilder();
      builder.appendField("chartId", chartId);
      try {
        final JsonObjectBuilder.JsonObject data = getChartData();
        if (data == null) {
          // If the data is null we don't send the chart.
          return null;
        }
        builder.appendField("data", data);
      } catch (final Throwable t) {
        if (logErrors) {
          errorLogger.accept("Failed to get data for custom chart with id " + chartId, t);
        }
        return null;
      }
      return builder.build();
    }

    protected abstract JsonObjectBuilder.JsonObject getChartData() throws Exception;
  }

  public static class SingleLineChart extends CustomChart {

    private final Callable<Integer> callable;

    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public SingleLineChart(final String chartId, final Callable<Integer> callable) {
      super(chartId);
      this.callable = callable;
    }

    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      final int value = callable.call();
      if (value == 0) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("value", value).build();
    }
  }

  public static class SimplePie extends CustomChart {

    private final Callable<String> callable;

    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public SimplePie(final String chartId, final Callable<String> callable) {
      super(chartId);
      this.callable = callable;
    }

    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      final String value = callable.call();
      if (value == null || value.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("value", value).build();
    }
  }

  public static class DrilldownPie extends CustomChart {

    private final Callable<Map<String, Map<String, Integer>>> callable;

    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */
    public DrilldownPie(final String chartId, final Callable<Map<String, Map<String, Integer>>> callable) {
      super(chartId);
      this.callable = callable;
    }

    @Override
    public JsonObjectBuilder.JsonObject getChartData() throws Exception {
      final JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      final Map<String, Map<String, Integer>> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      boolean reallyAllSkipped = true;
      for (final Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
        final JsonObjectBuilder valueBuilder = new JsonObjectBuilder();
        boolean allSkipped = true;
        for (final Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
          valueBuilder.appendField(valueEntry.getKey(), valueEntry.getValue());
          allSkipped = false;
        }
        if (!allSkipped) {
          reallyAllSkipped = false;
          valuesBuilder.appendField(entryValues.getKey(), valueBuilder.build());
        }
      }
      if (reallyAllSkipped) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

  /**
   * An extremely simple JSON builder.
   *
   * <p>While this class is neither feature-rich nor the most performant one, it's sufficient enough
   * for its use-case.
   */
  public static class JsonObjectBuilder {

    private StringBuilder builder = new StringBuilder();

    private boolean hasAtLeastOneField = false;

    public JsonObjectBuilder() {
      builder.append("{");
    }

    /**
     * Appends a null field to the JSON.
     *
     * @param key The key of the field.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendNull(final String key) {
      appendFieldUnescaped(key, "null");
      return this;
    }

    /**
     * Appends a string field to the JSON.
     *
     * @param key The key of the field.
     * @param value The value of the field.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(final String key, final String value) {
      if (value == null) {
        throw new IllegalArgumentException("JSON value must not be null");
      }
      appendFieldUnescaped(key, "\"" + escape(value) + "\"");
      return this;
    }

    /**
     * Appends an integer field to the JSON.
     *
     * @param key The key of the field.
     * @param value The value of the field.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(final String key, final int value) {
      appendFieldUnescaped(key, String.valueOf(value));
      return this;
    }

    /**
     * Appends an object to the JSON.
     *
     * @param key The key of the field.
     * @param object The object.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(final String key, final JsonObject object) {
      if (object == null) {
        throw new IllegalArgumentException("JSON object must not be null");
      }
      appendFieldUnescaped(key, object.toString());
      return this;
    }

    /**
     * Appends a string array to the JSON.
     *
     * @param key The key of the field.
     * @param values The string array.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(final String key, final String[] values) {
      if (values == null) {
        throw new IllegalArgumentException("JSON values must not be null");
      }
      final String escapedValues =
          Arrays.stream(values)
              .map(value -> "\"" + escape(value) + "\"")
              .collect(Collectors.joining(","));
      appendFieldUnescaped(key, "[" + escapedValues + "]");
      return this;
    }

    /**
     * Appends an integer array to the JSON.
     *
     * @param key The key of the field.
     * @param values The integer array.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(final String key, final int[] values) {
      if (values == null) {
        throw new IllegalArgumentException("JSON values must not be null");
      }
      final String escapedValues =
          Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
      appendFieldUnescaped(key, "[" + escapedValues + "]");
      return this;
    }

    /**
     * Appends an object array to the JSON.
     *
     * @param key The key of the field.
     * @param values The integer array.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(final String key, final JsonObject[] values) {
      if (values == null) {
        throw new IllegalArgumentException("JSON values must not be null");
      }
      final String escapedValues =
          Arrays.stream(values).map(JsonObject::toString).collect(Collectors.joining(","));
      appendFieldUnescaped(key, "[" + escapedValues + "]");
      return this;
    }

    /**
     * Appends a field to the object.
     *
     * @param key The key of the field.
     * @param escapedValue The escaped value of the field.
     */
    private void appendFieldUnescaped(final String key, final String escapedValue) {
      if (builder == null) {
        throw new IllegalStateException("JSON has already been built");
      }
      if (key == null) {
        throw new IllegalArgumentException("JSON key must not be null");
      }
      if (hasAtLeastOneField) {
        builder.append(",");
      }
      builder.append("\"").append(escape(key)).append("\":").append(escapedValue);
      hasAtLeastOneField = true;
    }

    /**
     * Builds the JSON string and invalidates this builder.
     *
     * @return The built JSON string.
     */
    public JsonObject build() {
      if (builder == null) {
        throw new IllegalStateException("JSON has already been built");
      }
      final JsonObject object = new JsonObject(builder.append("}").toString());
      builder = null;
      return object;
    }

    /**
     * Escapes the given string like stated in https://www.ietf.org/rfc/rfc4627.txt.
     *
     * <p>This method escapes only the necessary characters '"', '\'. and '\u0000' - '\u001F'.
     * Compact escapes are not used (e.g., '\n' is escaped as "\u000a" and not as "\n").
     *
     * @param value The value to escape.
     * @return The escaped value.
     */
    private static String escape(final String value) {
      final StringBuilder builder = new StringBuilder();
      for (int i = 0; i < value.length(); i++) {
        final char c = value.charAt(i);
        if (c == '"') {
          builder.append("\\\"");
        } else if (c == '\\') {
          builder.append("\\\\");
        } else if (c <= '\u000F') {
          builder.append("\\u000").append(Integer.toHexString(c));
        } else if (c <= '\u001F') {
          builder.append("\\u00").append(Integer.toHexString(c));
        } else {
          builder.append(c);
        }
      }
      return builder.toString();
    }

    /**
     * A super simple representation of a JSON object.
     *
     * <p>This class only exists to make methods of the {@link JsonObjectBuilder} type-safe and not
     * allow a raw string inputs for methods like {@link JsonObjectBuilder#appendField(String,
     * JsonObject)}.
     */
    public static class JsonObject {

      private final String value;

      private JsonObject(final String value) {
        this.value = value;
      }

      @Override
      public String toString() {
        return value;
      }
    }
  }
}