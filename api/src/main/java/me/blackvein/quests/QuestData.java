/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests;

import me.blackvein.quests.player.IQuester;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Quest objective data for a specified Quester
 */
public class QuestData {

    private final IQuester quester;

    public QuestData(final IQuester quester) {
        this.quester = quester;
    }
    
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

    public LinkedList<Boolean> npcsInteracted = new LinkedList<Boolean>() {

        private static final long serialVersionUID = 2447610341508300847L;

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

    public LinkedList<Integer> npcsNumKilled = new LinkedList<Integer>() {

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

    public LinkedList<Integer> mobsTamed = new LinkedList<Integer>() {

        private static final long serialVersionUID = 3851959471748032699L;

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

    private int fishCaught = 0;

    private int cowsMilked = 0;

    public LinkedList<Integer> sheepSheared = new LinkedList<Integer>() {

        private static final long serialVersionUID = -6016463677133534885L;

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

    private int playersKilled = 0;
    
    public LinkedList<Boolean> locationsReached = new LinkedList<Boolean>() {

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
    
    public LinkedList<Boolean> passwordsSaid = new LinkedList<Boolean>() {

        private static final long serialVersionUID = -4297291041298491402L;

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

    public LinkedList<Integer> customObjectiveCounts = new LinkedList<Integer>() {

        private static final long serialVersionUID = 6027656575740406823L;

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

    private long delayStartTime = 0;
    private long delayTimeLeft = -1;
    private boolean doJournalUpdate = true;

    public LinkedList<ItemStack> getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(final LinkedList<ItemStack> blocksBroken) {
        this.blocksBroken = blocksBroken;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getBlocksDamaged() {
        return blocksDamaged;
    }

    public void setBlocksDamaged(final LinkedList<ItemStack> blocksDamaged) {
        this.blocksDamaged = blocksDamaged;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getBlocksPlaced() {
        return blocksPlaced;
    }

    public void setBlocksPlaced(final LinkedList<ItemStack> blocksPlaced) {
        this.blocksPlaced = blocksPlaced;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getBlocksUsed() {
        return blocksUsed;
    }

    public void setBlocksUsed(final LinkedList<ItemStack> blocksUsed) {
        this.blocksUsed = blocksUsed;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getBlocksCut() {
        return blocksCut;
    }

    public void setBlocksCut(final LinkedList<ItemStack> blocksCut) {
        this.blocksCut = blocksCut;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getItemsCrafted() {
        return itemsCrafted;
    }

    public void setItemsCrafted(final LinkedList<ItemStack> itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getItemsSmelted() {
        return itemsSmelted;
    }

    public void setItemsSmelted(final LinkedList<ItemStack> itemsSmelted) {
        this.itemsSmelted = itemsSmelted;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getItemsEnchanted() {
        return itemsEnchanted;
    }

    public void setItemsEnchanted(final LinkedList<ItemStack> itemsEnchanted) {
        this.itemsEnchanted = itemsEnchanted;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getItemsBrewed() {
        return itemsBrewed;
    }

    public void setItemsBrewed(final LinkedList<ItemStack> itemsBrewed) {
        this.itemsBrewed = itemsBrewed;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getItemsConsumed() {
        return itemsConsumed;
    }

    public void setItemsConsumed(final LinkedList<ItemStack> itemsConsumed) {
        this.itemsConsumed = itemsConsumed;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<ItemStack> getItemsDelivered() {
        return itemsDelivered;
    }

    public void setItemsDelivered(final LinkedList<ItemStack> itemsDelivered) {
        this.itemsDelivered = itemsDelivered;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<Boolean> getNpcsInteracted() {
        return npcsInteracted;
    }

    public void setNpcsInteracted(final LinkedList<Boolean> npcsInteracted) {
        this.npcsInteracted = npcsInteracted;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<Integer> getNpcsNumKilled() {
        return npcsNumKilled;
    }

    public void setNpcsNumKilled(final LinkedList<Integer> npcsNumKilled) {
        this.npcsNumKilled = npcsNumKilled;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<Integer> getMobNumKilled() {
        return mobNumKilled;
    }

    public void setMobNumKilled(final LinkedList<Integer> mobNumKilled) {
        this.mobNumKilled = mobNumKilled;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<Integer> getMobsTamed() {
        return mobsTamed;
    }

    public void setMobsTamed(final LinkedList<Integer> mobsTamed) {
        this.mobsTamed = mobsTamed;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public int getFishCaught() {
        return fishCaught;
    }

    public void setFishCaught(final int fishCaught) {
        this.fishCaught = fishCaught;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public int getCowsMilked() {
        return cowsMilked;
    }
    
    public void setCowsMilked(final int cowsMilked) {
        this.cowsMilked = cowsMilked;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }
    
    public LinkedList<Integer> getSheepSheared() {
        return sheepSheared;
    }

    public void setSheepSheared(final LinkedList<Integer> sheepSheared) {
        this.sheepSheared = sheepSheared;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }
    
    public int getPlayersKilled() {
        return playersKilled;
    }
    
    public void setPlayersKilled(final int playersKilled) {
        this.playersKilled = playersKilled;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<Boolean> getLocationsReached() {
        return locationsReached;
    }

    public void setLocationsReached(final LinkedList<Boolean> locationsReached) {
        this.locationsReached = locationsReached;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public LinkedList<Boolean> getPasswordsSaid() {
        return passwordsSaid;
    }

    public void setPasswordsSaid(final LinkedList<Boolean> passwordsSaid) {
        this.passwordsSaid = passwordsSaid;
    }

    public LinkedList<Integer> getCustomObjectiveCounts() {
        return customObjectiveCounts;
    }

    public void setCustomObjectiveCounts(final LinkedList<Integer> customObjectiveCounts) {
        this.customObjectiveCounts = customObjectiveCounts;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }
    
    public long getDelayStartTime() {
        return delayStartTime;
    }
    
    public void setDelayStartTime(final long delayStartTime) {
        this.delayStartTime = delayStartTime;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }
    
    public long getDelayTimeLeft() {
        return delayTimeLeft;
    }
    
    public void setDelayTimeLeft(final long delayTimeLeft) {
        this.delayTimeLeft = delayTimeLeft;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    public boolean canDoJournalUpdate() {
        return doJournalUpdate;
    }

    public void setDoJournalUpdate(final boolean b) {
        doJournalUpdate = b;
    }
}
