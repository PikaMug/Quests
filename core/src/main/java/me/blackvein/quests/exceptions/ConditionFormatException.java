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

package me.blackvein.quests.exceptions;

public class ConditionFormatException extends Exception {

    private static final long serialVersionUID = 6165516939621807530L;
    private final String message;
    private final String conditionId;

    public ConditionFormatException(final String message, final String conditionId) {
        super(message + ", see condition of ID " + conditionId);
        this.message = message + ", see condition of ID " + conditionId;
        this.conditionId = conditionId;
    }
    
    /**
     * Get the message associated with this exception.
     * 
     * @return The message.
     */
    @Override
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the condition ID associated with this exception.
     * 
     * @return The condition that an invalid value was set within.
     */
    public String getConditionId() {
        return conditionId;
    }
}
