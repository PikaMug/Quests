/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.events.editor.actions;

import org.bukkit.conversations.ConversationContext;

import me.blackvein.quests.events.QuestsEvent;

/**
 * Represents an Actions Editor-related event
 */
public abstract class ActionsEditorEvent extends QuestsEvent {
    protected ConversationContext context;
    
    public ActionsEditorEvent(final ConversationContext context) {
        this.context = context;
    }
    
    public ActionsEditorEvent(final ConversationContext context, boolean async) {
        super(async);
        this.context = context;
    }
    
    /**
     * Returns the context involved in this event
     * 
     * @return ConversationContext which is involved in this event
     */
    public final ConversationContext getConversationContext() {
        return context;
    }
}
