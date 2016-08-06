package me.blackvein.quests;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class QuestData {

    private final Quester quester;
    private boolean doJournalUpdate = true;
    
    public QuestData(Quester quester) {
        this.quester = quester;
    }
    
    public void setDoJournalUpdate(boolean b) {
        doJournalUpdate = b;
    }
    
    public LinkedList<ItemStack> blocksDamaged = new LinkedList<ItemStack>() {
        
		private static final long serialVersionUID = -4211891633163257743L;

		@Override
        public ItemStack set(int index, ItemStack key) {
            ItemStack data = super.set(index, key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }
		
		@Override
        public boolean add(ItemStack key) {
            boolean data = super.add(key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(Object key) {
            boolean i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends ItemStack> m) {
            boolean i = super.addAll(m);
            if (doJournalUpdate) quester.updateJournal();
			return i;
        }
        
    };
    
    
    public LinkedList<ItemStack> blocksBroken = new LinkedList<ItemStack>() {
    	
		private static final long serialVersionUID = -6071822509475270168L;

		@Override
        public ItemStack set(int index, ItemStack key) {
            ItemStack data = super.set(index, key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }
		
		@Override
        public boolean add(ItemStack key) {
            boolean data = super.add(key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(Object key) {
            boolean i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends ItemStack> m) {
            boolean i = super.addAll(m);
            if (doJournalUpdate) quester.updateJournal();
			return i;
        }
        
    };
    
    
    public LinkedList<ItemStack> blocksPlaced = new LinkedList<ItemStack>() {
        
		private static final long serialVersionUID = 4226366446050903433L;

		@Override
        public ItemStack set(int index, ItemStack key) {
            ItemStack data = super.set(index, key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }
		
		@Override
        public boolean add(ItemStack key) {
            boolean data = super.add(key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(Object key) {
            boolean i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends ItemStack> m) {
            boolean i = super.addAll(m);
            if (doJournalUpdate) quester.updateJournal();
			return i;
        }
        
    };
    
    
    public LinkedList<ItemStack> blocksUsed = new LinkedList<ItemStack>() {
        
		private static final long serialVersionUID = -9057864863810306890L;

		@Override
        public ItemStack set(int index, ItemStack key) {
            ItemStack data = super.set(index, key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }
		
		@Override
        public boolean add(ItemStack key) {
            boolean data = super.add(key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(Object key) {
            boolean i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends ItemStack> m) {
            boolean i = super.addAll(m);
            if (doJournalUpdate) quester.updateJournal();
			return i;
        }
        
    };
    
    
    public LinkedList<ItemStack> blocksCut = new LinkedList<ItemStack>() {
        
		private static final long serialVersionUID = -8204359763290995080L;

		@Override
        public ItemStack set(int index, ItemStack key) {
            ItemStack data = super.set(index, key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }
		
		@Override
        public boolean add(ItemStack key) {
            boolean data = super.add(key);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(Object key) {
            boolean i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends ItemStack> m) {
            boolean i = super.addAll(m);
            if (doJournalUpdate) quester.updateJournal();
			return i;
        }
        
    };
    
    
    public Map<String, Integer> potionsBrewed = new HashMap<String, Integer>() {
        
		private static final long serialVersionUID = 5079308756224324031L;

		@Override
        public Integer put(String key, Integer val) {
            Integer data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    
    public Map<ItemStack, Integer> itemsDelivered = new HashMap<ItemStack, Integer>() {
        
		private static final long serialVersionUID = 2712497347022734646L;

		@Override
        public Integer put(ItemStack key, Integer val) {
            Integer data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends ItemStack, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    
    public Map<UUID, Long> playerKillTimes = new HashMap<UUID, Long>() {
        
		private static final long serialVersionUID = 9069594419068019465L;

		@Override
        public Long put(UUID key, Long val) {
            Long data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Long remove(Object key) {
            Long i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends UUID, ? extends Long> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    
    public Map<Map<Enchantment, Material>, Integer> itemsEnchanted = new HashMap<Map<Enchantment, Material>, Integer>() {
        
		private static final long serialVersionUID = 416869352279205852L;

		@Override
        public Integer put(Map<Enchantment, Material> key, Integer val) {
            Integer data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends Map<Enchantment, Material>, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    
    public LinkedList<EntityType> mobsKilled = new LinkedList<EntityType>() {

		private static final long serialVersionUID = 8178007458817522183L;

		@Override
        public boolean add(EntityType e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, EntityType element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends EntityType> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends EntityType> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public EntityType remove(int index) {
            EntityType s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public EntityType set(int index, EntityType element) {
            EntityType s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    public LinkedList<Integer> mobNumKilled = new LinkedList<Integer>() {

		private static final long serialVersionUID = 2228385647091499176L;

		@Override
        public boolean add(Integer e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, Integer element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends Integer> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Integer> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(int index) {
            Integer s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(int index, Integer element) {
            Integer s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    
    public LinkedList<Location> locationsToKillWithin = new LinkedList<Location>() {

		private static final long serialVersionUID = 557285564460615021L;

		@Override
        public boolean add(Location e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, Location element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends Location> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Location> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public Location remove(int index) {
            Location s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public Location set(int index, Location element) {
            Location s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    
    public LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>() {

		private static final long serialVersionUID = 1973115869697752181L;

		@Override
        public boolean add(Integer e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, Integer element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends Integer> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Integer> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(int index) {
            Integer s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(int index, Integer element) {
            Integer s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    
    public Map<Integer, Boolean> citizensInteracted = new HashMap<Integer, Boolean>() {
        
		private static final long serialVersionUID = 2447610341508300847L;

		@Override
        public Boolean put(Integer key, Boolean val) {
            Boolean data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Boolean remove(Object key) {
            Boolean i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends Integer, ? extends Boolean> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
            
            
    public LinkedList<Integer> citizensKilled = new LinkedList<Integer>() {

		private static final long serialVersionUID = -6054581494356961482L;

		@Override
        public boolean add(Integer e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, Integer element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends Integer> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Integer> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(int index) {
            Integer s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(int index, Integer element) {
            Integer s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    
    public LinkedList<Integer> citizenNumKilled = new LinkedList<Integer>() {

		private static final long serialVersionUID = 1849192351499071688L;

		@Override
        public boolean add(Integer e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, Integer element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends Integer> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Integer> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(int index) {
            Integer s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(int index, Integer element) {
            Integer s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    
    public LinkedList<Location> locationsReached = new LinkedList<Location>() {

		private static final long serialVersionUID = 2875034788869133862L;

		@Override
        public boolean add(Location e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, Location element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends Location> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Location> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public Location remove(int index) {
            Location s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public Location set(int index, Location element) {
            Location s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    
    public LinkedList<Boolean> hasReached = new LinkedList<Boolean>() {

		private static final long serialVersionUID = -8802305642082466541L;

		@Override
        public boolean add(Boolean e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, Boolean element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends Boolean> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Boolean> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public Boolean remove(int index) {
            Boolean s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public Boolean set(int index, Boolean element) {
            Boolean s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    
    public LinkedList<Integer> radiiToReachWithin = new LinkedList<Integer>() {

		private static final long serialVersionUID = 6027656509740406846L;

		@Override
        public boolean add(Integer e) {
            boolean b = super.add(e);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void add(int index, Integer element) {
            super.add(index, element);
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends Integer> c) {
            boolean b = super.addAll(c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Integer> c) {
            boolean b = super.addAll(index, c);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            if (doJournalUpdate) quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(int index) {
            Integer s = super.remove(index);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(int index, Integer element) {
            Integer s = super.set(index, element);
            if (doJournalUpdate) quester.updateJournal();
            return s;
        }
        
    };
    
    
    public Map<EntityType, Integer> mobsTamed = new EnumMap<EntityType, Integer>(EntityType.class) {
        
		private static final long serialVersionUID = 3851959471748032699L;

		@Override
        public Integer put(EntityType key, Integer val) {
            Integer data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends EntityType, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    
    public Map<DyeColor, Integer> sheepSheared = new EnumMap<DyeColor, Integer>(DyeColor.class) {
        
		private static final long serialVersionUID = -6016463677133534885L;

		@Override
        public Integer put(DyeColor key, Integer val) {
            Integer data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends DyeColor, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    
    public Map<String, Boolean> passwordsSaid = new HashMap<String, Boolean>() {
        
		private static final long serialVersionUID = -4297290041298491402L;

		@Override
        public Boolean put(String key, Boolean val) {
            Boolean data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Boolean remove(Object key) {
            Boolean i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Boolean> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    public Map<String, Integer> customObjectiveCounts = new HashMap<String, Integer>() {
        
		private static final long serialVersionUID = -2148775183072606256L;

		@Override
        public Integer put(String key, Integer val) {
            Integer data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    
    public Map<String, Boolean> eventFired = new HashMap<String, Boolean>() {
        
		private static final long serialVersionUID = 7106048037834965123L;

		@Override
        public Boolean put(String key, Boolean val) {
            Boolean data = super.put(key, val);
            if (doJournalUpdate) quester.updateJournal();
            return data;
        }

        @Override
        public Boolean remove(Object key) {
            Boolean i = super.remove(key);
            if (doJournalUpdate) quester.updateJournal();
            return i;
        }
        
        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate) quester.updateJournal();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Boolean> m) {
            super.putAll(m);
            if (doJournalUpdate) quester.updateJournal();
        }
        
    };
    
    
    private int fishCaught = 0;
    private int playersKilled = 0;
    public long delayStartTime = 0;
    public long delayTimeLeft = -1;
    public boolean delayOver = true;
    
    public void setFishCaught(int i) {
        fishCaught = i;
        if (doJournalUpdate) quester.updateJournal();
    }
    
    public void setPlayersKilled(int i) {
        playersKilled = i;
        if (doJournalUpdate) quester.updateJournal();
    }

    public int getFishCaught() {
        return fishCaught;
    }

    public int getPlayersKilled() {
        return playersKilled;
    }

}
