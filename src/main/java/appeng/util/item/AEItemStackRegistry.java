/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.util.item;

import com.google.common.collect.MapMaker;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AEItemStackRegistry {

    private static final ItemStackHashStrategy HASH_STRATEGY = ItemStackHashStrategy.comparingAllButCount();
    // Use a bucketed map to avoid incorrect collisions when two different ItemStacks produce the same hash.
    private static final Map<Integer, List<AESharedItemStack>> REGISTRY = new MapMaker().makeMap();

    private AEItemStackRegistry() {
    }

    static synchronized AESharedItemStack getRegisteredStack(final @Nonnull ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            throw new IllegalArgumentException("stack cannot be empty");
        }

        var hash = HASH_STRATEGY.hashCode(itemStack);

        // Copy and normalize the stack used for key comparisons.
        var itemStackCopy = itemStack.copy();
        itemStackCopy.setCount(1);

        // Look up existing bucket for this hash.
        List<AESharedItemStack> bucket = REGISTRY.get(hash);
        if (bucket != null) {
            for (final AESharedItemStack s : bucket) {
                if (ItemStack.areItemStacksEqual(s.getDefinition(), itemStackCopy)) {
                    return s;
                }
            }
            // No equal entry found in bucket; create and append.
            var sharedStack = new AESharedItemStack(itemStackCopy);
            bucket.add(sharedStack);
            return sharedStack;
        }

        // No bucket yet for this hash; create one and insert the new shared stack.
        var sharedStack = new AESharedItemStack(itemStackCopy);
        var list = new ArrayList<AESharedItemStack>();
        list.add(sharedStack);
        REGISTRY.put(hash, list);
        return sharedStack;
    }
}
