/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.player;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Quest objective progress for a specified Quester
 */
public class BukkitQuestProgress implements QuestProgress {

    private final Quester quester;

    public BukkitQuestProgress(final Quester quester) {
        this.quester = quester;
    }

    public LinkedList<Integer> blocksBroken = new LinkedList<Integer>() {

        private static final long serialVersionUID = -6071822509475270169L;

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

    public LinkedList<Integer> blocksDamaged = new LinkedList<Integer>() {

        private static final long serialVersionUID = -4211891633163257744L;

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
    
    public LinkedList<Integer> blocksPlaced = new LinkedList<Integer>() {

        private static final long serialVersionUID = 4226366446050903434L;

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
    
    public LinkedList<Integer> blocksUsed = new LinkedList<Integer>() {

        private static final long serialVersionUID = -9057864863810306891L;

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
    
    public LinkedList<Integer> blocksCut = new LinkedList<Integer>() {

        private static final long serialVersionUID = -8204359763290995081L;

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
    
    public LinkedList<Integer> itemsCrafted = new LinkedList<Integer>() {

        private static final long serialVersionUID = 2774356294049526106L;

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
    
    public LinkedList<Integer> itemsSmelted = new LinkedList<Integer>() {

        private static final long serialVersionUID = 2774356235274526107L;

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
    
    public LinkedList<Integer> itemsEnchanted = new LinkedList<Integer>() {

        private static final long serialVersionUID = 416869352279205853L;

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
    
    public LinkedList<Integer> itemsBrewed = new LinkedList<Integer>() {

        private static final long serialVersionUID = 2774356235274526108L;

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
    
    public LinkedList<Integer> itemsConsumed = new LinkedList<Integer>() {

        private static final long serialVersionUID = -5475073316902757884L;

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
    
    public LinkedList<Integer> itemsDelivered = new LinkedList<Integer>() {

        private static final long serialVersionUID = 2712497347022734647L;

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

    @Override
    public LinkedList<Integer> getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(final LinkedList<Integer> blocksBroken) {
        this.blocksBroken = blocksBroken;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getBlocksDamaged() {
        return blocksDamaged;
    }

    public void setBlocksDamaged(final LinkedList<Integer> blocksDamaged) {
        this.blocksDamaged = blocksDamaged;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getBlocksPlaced() {
        return blocksPlaced;
    }

    public void setBlocksPlaced(final LinkedList<Integer> blocksPlaced) {
        this.blocksPlaced = blocksPlaced;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getBlocksUsed() {
        return blocksUsed;
    }

    public void setBlocksUsed(final LinkedList<Integer> blocksUsed) {
        this.blocksUsed = blocksUsed;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getBlocksCut() {
        return blocksCut;
    }

    public void setBlocksCut(final LinkedList<Integer> blocksCut) {
        this.blocksCut = blocksCut;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getItemsCrafted() {
        return itemsCrafted;
    }

    public void setItemsCrafted(final LinkedList<Integer> itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getItemsSmelted() {
        return itemsSmelted;
    }

    public void setItemsSmelted(final LinkedList<Integer> itemsSmelted) {
        this.itemsSmelted = itemsSmelted;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getItemsEnchanted() {
        return itemsEnchanted;
    }

    public void setItemsEnchanted(final LinkedList<Integer> itemsEnchanted) {
        this.itemsEnchanted = itemsEnchanted;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getItemsBrewed() {
        return itemsBrewed;
    }

    public void setItemsBrewed(final LinkedList<Integer> itemsBrewed) {
        this.itemsBrewed = itemsBrewed;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getItemsConsumed() {
        return itemsConsumed;
    }

    public void setItemsConsumed(final LinkedList<Integer> itemsConsumed) {
        this.itemsConsumed = itemsConsumed;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getItemsDelivered() {
        return itemsDelivered;
    }

    public void setItemsDelivered(final LinkedList<Integer> itemsDelivered) {
        this.itemsDelivered = itemsDelivered;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Boolean> getNpcsInteracted() {
        return npcsInteracted;
    }

    @Override
    public void setNpcsInteracted(final LinkedList<Boolean> npcsInteracted) {
        this.npcsInteracted = npcsInteracted;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getNpcsNumKilled() {
        return npcsNumKilled;
    }

    @Override
    public void setNpcsNumKilled(final LinkedList<Integer> npcsNumKilled) {
        this.npcsNumKilled = npcsNumKilled;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getMobNumKilled() {
        return mobNumKilled;
    }

    @Override
    public void setMobNumKilled(final LinkedList<Integer> mobNumKilled) {
        this.mobNumKilled = mobNumKilled;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getMobsTamed() {
        return mobsTamed;
    }

    @Override
    public void setMobsTamed(final LinkedList<Integer> mobsTamed) {
        this.mobsTamed = mobsTamed;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public int getFishCaught() {
        return fishCaught;
    }

    @Override
    public void setFishCaught(final int fishCaught) {
        this.fishCaught = fishCaught;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public int getCowsMilked() {
        return cowsMilked;
    }

    @Override
    public void setCowsMilked(final int cowsMilked) {
        this.cowsMilked = cowsMilked;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Integer> getSheepSheared() {
        return sheepSheared;
    }

    @Override
    public void setSheepSheared(final LinkedList<Integer> sheepSheared) {
        this.sheepSheared = sheepSheared;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public int getPlayersKilled() {
        return playersKilled;
    }

    @Override
    public void setPlayersKilled(final int playersKilled) {
        this.playersKilled = playersKilled;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Boolean> getLocationsReached() {
        return locationsReached;
    }

    @Override
    public void setLocationsReached(final LinkedList<Boolean> locationsReached) {
        this.locationsReached = locationsReached;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public LinkedList<Boolean> getPasswordsSaid() {
        return passwordsSaid;
    }

    @Override
    public void setPasswordsSaid(final LinkedList<Boolean> passwordsSaid) {
        this.passwordsSaid = passwordsSaid;
    }

    @Override
    public LinkedList<Integer> getCustomObjectiveCounts() {
        return customObjectiveCounts;
    }

    @Override
    public void setCustomObjectiveCounts(final LinkedList<Integer> customObjectiveCounts) {
        this.customObjectiveCounts = customObjectiveCounts;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public long getDelayStartTime() {
        return delayStartTime;
    }

    @Override
    public void setDelayStartTime(final long delayStartTime) {
        this.delayStartTime = delayStartTime;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public long getDelayTimeLeft() {
        return delayTimeLeft;
    }

    @Override
    public void setDelayTimeLeft(final long delayTimeLeft) {
        this.delayTimeLeft = delayTimeLeft;
        if (doJournalUpdate) {
            quester.updateJournal();
        }
    }

    @Override
    public boolean canDoJournalUpdate() {
        return doJournalUpdate;
    }

    @Override
    public void setDoJournalUpdate(final boolean b) {
        doJournalUpdate = b;
    }
}
