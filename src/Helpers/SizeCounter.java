/* Copyright (c) 2016 UDBMS group, Department of Computer Science, University of Helsinki
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package Helpers;

import Helpers.DFUDSString;
import Helpers.TrieNode;

public class SizeCounter {
    public static int Count(TrieNode root, boolean withDelta) {
        final int[] size = {0};

        size[0] += CountSingle(root, withDelta);

        // count children
        root.children.values().forEach(c -> size[0] += Count(c, withDelta));

        return size[0];
    }

    public static int CountSingle(TrieNode node, boolean withDelta) {
        int size = 0;

        // count self
        size += 1;// root.value
        size += 4;// root.score
        size += 4;//root.parent.children
        size += 4;//root.parent
        size += node.links.size() * (withDelta ? 4 * 2 : 4);

        return size;
    }

    public static int CountSynonym(TrieNode root, boolean withDelta) {
        final int[] size = {0};

        if (root.score == 0) {
            size[0] = CountSingle(root, withDelta);
        }

        // count children
        root.children.values().forEach(c -> size[0] += CountSynonym(c, withDelta));

        return size[0];
    }

    public static int CountDict(TrieNode root) {
        if (root.score == 0)
            return 0;

        final int[] size = {0};

        size[0] = CountSingle(root, false);

        // count children
        root.children.values().forEach(c -> size[0] += CountDict(c));

        return size[0];
    }

    public static int CountNode(TrieNode root) {
        final int[] size = {0};

        // count self
        size[0] += 1;// root.value
        size[0] += 4;// root.score

        // count children
        root.children.values().forEach(c -> size[0] += CountNode(c));

        return size[0];
    }

    public static int CountRelation(TrieNode root) {
        final int[] size = {0};

        // count self
        size[0] += 4;//root.parent.children
        size[0] += 4;//root.parent

        // count children
        root.children.values().forEach(c -> size[0] += CountRelation(c));

        return size[0];
    }

    public static int CountLink(TrieNode root) {
        final int[] size = {0};

        // count self
        size[0] += root.links.size() * 4 * 2;

        // count children
        root.children.values().forEach(c -> size[0] += CountLink(c));

        return size[0];
    }

    public static int Count(DFUDSString dfuds) {
        final int[] size = {0};

        size[0] += dfuds.Parenthesis.length() / 8;
        size[0] += dfuds.Labels.length();
        size[0] += dfuds.Ranks.length * 4;

        dfuds.Links.forEach(l -> size[0] += l.size() * 4 * 2);

        return size[0];
    }

    public static int CountNode(DFUDSString dfuds) {
        final int[] size = {0};

        size[0] += dfuds.Parenthesis.length() / 8;
        size[0] += dfuds.Labels.length();
        size[0] += dfuds.Ranks.length * 4;

        return size[0];
    }

    public static int CountRelation(DFUDSString dfuds) {
        final int[] size = {0};

        return size[0];
    }

    public static int CountLink(DFUDSString dfuds) {
        final int[] size = {0};

        dfuds.Links.forEach(l -> size[0] += l.size() * 4 * 2);

        return size[0];
    }
}
