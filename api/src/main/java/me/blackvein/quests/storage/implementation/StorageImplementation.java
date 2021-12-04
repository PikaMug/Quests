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

package me.blackvein.quests.storage.implementation;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;

import java.util.Collection;
import java.util.UUID;

public interface StorageImplementation {
    Quests getPlugin();

    String getImplementationName();

    void init() throws Exception;

    void close();
    
    Quester loadQuester(UUID uniqueId) throws Exception;

    void saveQuester(Quester quester) throws Exception;

    void deleteQuester(UUID uniqueId) throws Exception;

    String getQuesterLastKnownName(UUID uniqueId) throws Exception;
    
    Collection<UUID> getSavedUniqueIds() throws Exception;
}
