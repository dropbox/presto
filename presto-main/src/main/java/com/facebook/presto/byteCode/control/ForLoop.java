/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.byteCode.control;

import com.facebook.presto.byteCode.ByteCodeBlock;
import com.facebook.presto.byteCode.ByteCodeNode;
import com.facebook.presto.byteCode.ByteCodeVisitor;
import com.facebook.presto.byteCode.MethodGenerationContext;
import com.facebook.presto.byteCode.instruction.LabelNode;
import com.google.common.collect.ImmutableList;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public class ForLoop
        implements FlowControl
{
    private final String comment;
    private final ByteCodeBlock initialize = new ByteCodeBlock();
    private final ByteCodeBlock condition = new ByteCodeBlock();
    private final ByteCodeBlock update = new ByteCodeBlock();
    private final ByteCodeBlock body = new ByteCodeBlock();

    private final LabelNode beginLabel = new LabelNode("beginLabel");
    private final LabelNode continueLabel = new LabelNode("continue");
    private final LabelNode endLabel = new LabelNode("end");

    public ForLoop()
    {
        this.comment = null;
    }

    public ForLoop(String format, Object... args)
    {
        this.comment = String.format(format, args);
    }

    @Override
    public String getComment()
    {
        return comment;
    }

    public LabelNode getContinueLabel()
    {
        return continueLabel;
    }

    public LabelNode getEndLabel()
    {
        return endLabel;
    }

    public ByteCodeBlock initialize()
    {
        return initialize;
    }

    public ForLoop initialize(ByteCodeNode node)
    {
        checkState(initialize.isEmpty(), "initialize already set");
        initialize.append(node);
        return this;
    }

    public ByteCodeBlock condition()
    {
        return condition;
    }

    public ForLoop condition(ByteCodeNode node)
    {
        checkState(condition.isEmpty(), "condition already set");
        condition.append(node);
        return this;
    }

    public ByteCodeBlock update()
    {
        return update;
    }

    public ForLoop update(ByteCodeNode node)
    {
        checkState(update.isEmpty(), "update already set");
        update.append(node);
        return this;
    }

    public ByteCodeBlock body()
    {
        return body;
    }

    public ForLoop body(ByteCodeNode node)
    {
        checkState(body.isEmpty(), "body already set");
        body.append(node);
        return this;
    }

    @Override
    public void accept(MethodVisitor visitor, MethodGenerationContext generationContext)
    {
        checkState(!condition.isEmpty(), "ForLoop does not have a condition set");

        ByteCodeBlock block = new ByteCodeBlock();

        block.append(new ByteCodeBlock()
                .setDescription("initialize")
                .append(initialize));

        block.visitLabel(beginLabel)
                .append(new ByteCodeBlock()
                        .setDescription("condition")
                        .append(condition))
                .ifFalseGoto(endLabel);

        block.append(new ByteCodeBlock()
                .setDescription("body")
                .append(body));

        block.visitLabel(continueLabel)
                .append(new ByteCodeBlock()
                        .setDescription("update")
                        .append(update))
                .gotoLabel(beginLabel)
                .visitLabel(endLabel);

        block.accept(visitor, generationContext);
    }

    @Override
    public List<ByteCodeNode> getChildNodes()
    {
        return ImmutableList.of(initialize, condition, update, body);
    }

    @Override
    public <T> T accept(ByteCodeNode parent, ByteCodeVisitor<T> visitor)
    {
        return visitor.visitFor(parent, this);
    }
}
