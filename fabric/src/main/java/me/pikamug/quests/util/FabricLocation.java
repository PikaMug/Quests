package me.pikamug.quests.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class FabricLocation {
    private final ServerLevel world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public FabricLocation(final ServerLevel world, final double x, final double y, final double z) {
        this(world, x, y, z, 0.0f, 0.0f);
    }

    public FabricLocation(final ServerLevel world, final double x, final double y, final double z, final float yaw, final float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public FabricLocation(final ServerLevel world, final BlockPos pos) {
        this(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0f, 0.0f);
    }

    public ServerLevel getWorld() {
        return world;
    }

    public ServerLevel getLevel() {
        return world;
    }

    public String getWorldId() {
        return world.dimension().identifier().toString();
    }

    public String getLevelId() {
        return world.dimension().identifier().toString();
    }

    public double getX() {
        return x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(final double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }

    public BlockPos getBlockPos() {
        return BlockPos.containing(x, y, z);
    }

    public Vec3 toVec3() {
        return new Vec3(x, y, z);
    }

    public void add(final double x, final double y, final double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    @Override
    public FabricLocation clone() {
        return new FabricLocation(world, x, y, z, yaw, pitch);
    }

    public double distance(final FabricLocation loc) {
        if (loc == null || loc.world != this.world) {
            throw new IllegalArgumentException("Cannot measure distance between different worlds");
        }
        final double dx = this.x - loc.x;
        final double dy = this.y - loc.y;
        final double dz = this.z - loc.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double distanceSquared(final FabricLocation loc) {
        if (loc == null || loc.world != this.world) {
            throw new IllegalArgumentException("Cannot measure distance between different worlds");
        }
        final double dx = this.x - loc.x;
        final double dy = this.y - loc.y;
        final double dz = this.z - loc.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
