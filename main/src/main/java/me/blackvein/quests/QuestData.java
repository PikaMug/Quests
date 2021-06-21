/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class QuestData {

    private final Quester quester;
    private boolean doJournalUpdate = true;

    public QuestData(final Quester quester) {
        this.quester = quester;
    }

    public void setDoJournalUpdate(final boolean b) {
        doJournalUpdate = b;
    }

    public LinkedList<ItemStack> blocksDamaged = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = -4211891633163257743L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> blocksBroken = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = -6071822509475270168L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> blocksPlaced = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = 4226366446050903433L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> blocksUsed = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = -9057864863810306890L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> blocksCut = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = -8204359763290995080L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> itemsCrafted = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = 2774356294049526105L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> itemsSmelted = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = 2774356235274526106L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> itemsEnchanted = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = 416869352279205852L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> itemsBrewed = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = 2774356235274526107L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> itemsConsumed = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = -5475073316902757883L;
        
        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<ItemStack> itemsDelivered = new LinkedList<ItemStack>() {

        private static final long serialVersionUID = 2712497347022734646L;

        @Override
        public ItemStack set(final int index, final ItemStack key) {
            final ItemStack data = super.set(index, key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean add(final ItemStack key) {
            final boolean data = super.add(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public boolean remove(final Object key) {
            final boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends ItemStack> m) {
            final boolean i = super.addAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }
    };
    
    public LinkedList<EntityType> mobsKilled = new LinkedList<EntityType>() {

        private static final long serialVersionUID = 8178007458817522183L;

        @Override
        public boolean add(final EntityType e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final EntityType element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends EntityType> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends EntityType> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public EntityType remove(final int index) {
            final EntityType s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public EntityType set(final int index, final EntityType element) {
            final EntityType s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedList<Integer> mobNumKilled = new LinkedList<Integer>() {

        private static final long serialVersionUID = 2228385647091499176L;

        @Override
        public boolean add(final Integer e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final Integer element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends Integer> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Integer> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(final int index) {
            final Integer s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(final int index, final Integer element) {
            final Integer s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedList<Location> locationsToKillWithin = new LinkedList<Location>() {

        private static final long serialVersionUID = 557285564460615021L;

        @Override
        public boolean add(final Location e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final Location element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends Location> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Location> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public Location remove(final int index) {
            final Location s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public Location set(final int index, final Location element) {
            final Location s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>() {

        private static final long serialVersionUID = 1973115869697752181L;

        @Override
        public boolean add(final Integer e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final Integer element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends Integer> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Integer> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(final int index) {
            final Integer s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(final int index, final Integer element) {
            final Integer s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedHashMap<Integer, Boolean> citizensInteracted = new LinkedHashMap<Integer, Boolean>() {

        private static final long serialVersionUID = 2447610341508300847L;

        @Override
        public Boolean put(final Integer key, final Boolean val) {
            final Boolean data = super.put(key, val);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public Boolean remove(final Object key) {
            final Boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public void putAll(final Map<? extends Integer, ? extends Boolean> m) {
            super.putAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
        }
    };
    
    public LinkedList<Integer> citizensKilled = new LinkedList<Integer>() {

        private static final long serialVersionUID = -6054581494356961482L;

        @Override
        public boolean add(final Integer e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final Integer element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends Integer> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Integer> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(final int index) {
            final Integer s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(final int index, final Integer element) {
            final Integer s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedList<Integer> citizenNumKilled = new LinkedList<Integer>() {

        private static final long serialVersionUID = 1849192351499071688L;

        @Override
        public boolean add(final Integer e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final Integer element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends Integer> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Integer> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(final int index) {
            final Integer s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(final int index, final Integer element) {
            final Integer s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedList<Location> locationsReached = new LinkedList<Location>() {

        private static final long serialVersionUID = 2875034788869133862L;

        @Override
        public boolean add(final Location e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final Location element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends Location> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Location> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public Location remove(final int index) {
            final Location s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public Location set(final int index, final Location element) {
            final Location s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedList<Boolean> hasReached = new LinkedList<Boolean>() {

        private static final long serialVersionUID = -8802305642082466541L;

        @Override
        public boolean add(final Boolean e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final Boolean element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends Boolean> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Boolean> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public Boolean remove(final int index) {
            final Boolean s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public Boolean set(final int index, final Boolean element) {
            final Boolean s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedList<Integer> radiiToReachWithin = new LinkedList<Integer>() {

        private static final long serialVersionUID = 6027656509740406846L;

        @Override
        public boolean add(final Integer e) {
            final boolean b = super.add(e);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void add(final int index, final Integer element) {
            super.add(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean addAll(final Collection<? extends Integer> c) {
            final boolean b = super.addAll(c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Integer> c) {
            final boolean b = super.addAll(index, c);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            if (doJournalUpdate)
                quester.updateJournal();
            return b;
        }

        @Override
        public Integer remove(final int index) {
            final Integer s = super.remove(index);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }

        @Override
        public Integer set(final int index, final Integer element) {
            final Integer s = super.set(index, element);
            if (doJournalUpdate)
                quester.updateJournal();
            return s;
        }
    };
    
    public LinkedHashMap<EntityType, Integer> mobsTamed = new LinkedHashMap<EntityType, Integer>() {

        private static final long serialVersionUID = 3851959471748032699L;

        @Override
        public Integer put(final EntityType key, final Integer val) {
            final Integer data = super.put(key, val);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(final Object key) {
            final Integer i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public void putAll(final Map<? extends EntityType, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
        }
    };
    
    public LinkedHashMap<DyeColor, Integer> sheepSheared = new LinkedHashMap<DyeColor, Integer>() {

        private static final long serialVersionUID = -6016463677133534885L;

        @Override
        public Integer put(final DyeColor key, final Integer val) {
            final Integer data = super.put(key, val);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(final Object key) {
            final Integer i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public void putAll(final Map<? extends DyeColor, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
        }
    };
    
    public LinkedHashMap<String, Boolean> passwordsSaid = new LinkedHashMap<String, Boolean>() {

        private static final long serialVersionUID = -4297290041298491402L;

        @Override
        public Boolean put(final String key, final Boolean val) {
            final Boolean data = super.put(key, val);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public Boolean remove(final Object key) {
            final Boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public void putAll(final Map<? extends String, ? extends Boolean> m) {
            super.putAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
        }
    };
    
    public LinkedHashMap<String, Integer> customObjectiveCounts = new LinkedHashMap<String, Integer>() {

        private static final long serialVersionUID = -2148775183072606256L;

        @Override
        public Integer put(final String key, final Integer val) {
            final Integer data = super.put(key, val);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public Integer remove(final Object key) {
            final Integer i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public void putAll(final Map<? extends String, ? extends Integer> m) {
            super.putAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
        }
    };
    
    public LinkedHashMap<String, Boolean> actionFired = new LinkedHashMap<String, Boolean>() {

        private static final long serialVersionUID = 7106048037834965123L;

        @Override
        public Boolean put(final String key, final Boolean val) {
            final Boolean data = super.put(key, val);
            if (doJournalUpdate)
                quester.updateJournal();
            return data;
        }

        @Override
        public Boolean remove(final Object key) {
            final Boolean i = super.remove(key);
            if (doJournalUpdate)
                quester.updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            if (doJournalUpdate)
                quester.updateJournal();
        }

        @Override
        public void putAll(final Map<? extends String, ? extends Boolean> m) {
            super.putAll(m);
            if (doJournalUpdate)
                quester.updateJournal();
        }
    };
    
    private int cowsMilked = 0;
    private int fishCaught = 0;
    private int playersKilled = 0;
    private long delayStartTime = 0;
    private long delayTimeLeft = -1;
    //private final boolean delayOver = true;
    
    public int getCowsMilked() {
        return cowsMilked;
    }
    
    public void setCowsMilked(final int cowsMilked) {
        this.cowsMilked = cowsMilked;
        if (doJournalUpdate)
            quester.updateJournal();
    }
    
    public int getFishCaught() {
        return fishCaught;
    }
    
    public void setFishCaught(final int fishCaught) {
        this.fishCaught = fishCaught;
        if (doJournalUpdate)
            quester.updateJournal();
    }
    
    public int getPlayersKilled() {
        return playersKilled;
    }
    
    public void setPlayersKilled(final int playersKilled) {
        this.playersKilled = playersKilled;
        if (doJournalUpdate)
            quester.updateJournal();
    }
    
    public long getDelayStartTime() {
        return delayStartTime;
    }
    
    public void setDelayStartTime(final long delayStartTime) {
        this.delayStartTime = delayStartTime;
        if (doJournalUpdate)
            quester.updateJournal();
    }
    
    public long getDelayTimeLeft() {
        return delayTimeLeft;
    }
    
    public void setDelayTimeLeft(final long delayTimeLeft) {
        this.delayTimeLeft = delayTimeLeft;
        if (doJournalUpdate)
            quester.updateJournal();
    }
    
    /*public boolean isDelayOver() {
        return delayOver;
    }
    
    public void setDelayOver(final boolean delayOver) {
        this.delayOver = delayOver;
        if (doJournalUpdate)
            quester.updateJournal();
    }*/
}
