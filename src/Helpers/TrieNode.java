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

import java.util.ArrayList;
import java.util.HashMap;

public class TrieNode implements Comparable<TrieNode> {
    public char value = '$';
    public int score = -1;
    public TrieNode parent = null;
    public HashMap<Character, TrieNode> children = new HashMap<>();
    public HashMap<TrieNode, Integer> links = new HashMap<>();

    public TrieNode() {

    }

    /**
     * Get if this node has any link.
     */
    public boolean HasLink() {
        return links.size() != 0;
    }

    /**
     * Get if this node is from a synonym link.
     */
    public boolean IsLinkNode() {
        return score == 0;
    }

    /**
     * Find and return a child node
     *
     * @param value
     * @return null if node not found
     */
    public TrieNode FindChild(char value) {
        return this.children.get(value);
    }

    public TrieNode GoUp(int step) {
        if (this.parent == null)
            return this;

        TrieNode pointer = this;
        int c = 0;
        do {
            if (c == step)
                break;

            pointer = pointer.parent;
            c++;
        } while (pointer != null);

        return pointer;
    }

    public int GetDepth() {
        int depth = 0;

        TrieNode pointer = this.parent;
        while (pointer != null) {
            depth++;

            pointer = pointer.parent;
        }

        return depth;
    }

    /**
     * Find a node by a string. Current level is not included.
     *
     * @param str
     * @return
     */
    public TrieNode FindByString(String str) {
        TrieNode last = this;
        for (int i = 0; i < str.length(); i++) {
            last = last.FindChild(str.charAt(i));

            if (last == null) return null;
        }

        return last;
    }

    /**
     * Find nodes by a string. Current level is not included.
     *
     * @param str
     * @return
     */
    public TrieNode[] FindAllByString(String str) {
        ArrayList<TrieNode> result = new ArrayList<>();

        TrieNode last = this;
        for (int i = 0; i < str.length(); i++) {
            last = last.FindChild(str.charAt(i));

            result.add(last);

            if (last == null) return null;
        }

        return result.toArray(new TrieNode[result.size()]);
    }

    /**
     * Find deepest node by a string. Current level is not included.
     *
     * @param str
     * @return
     */
    public TrieNode FindLastMuchByString(String str) {
        TrieNode deepest = null;

        TrieNode last = this;
        for (int i = 0; i < str.length(); i++) {
            last = last.FindChild(str.charAt(i));

            if (last != null)
                deepest = last;
            else
                break;
        }

        return deepest;
    }

    /**
     * Find nodes by a string. Current level is not included.
     *
     * @param str
     * @return
     */
    public TrieNode[] FindMuchByString(String str) {
        ArrayList<TrieNode> result = new ArrayList<>();

        TrieNode last = this;
        for (int i = 0; i < str.length(); i++) {
            last = last.FindChild(str.charAt(i));

            if (last != null)
                result.add(last);
            else
                break;
        }

        return result.toArray(new TrieNode[result.size()]);
    }

    /**
     * Add a new link
     */
    public void AddLink(TrieNode to, int delta) {
        this.links.put(to, delta);
    }

    /**
     * Add a new child
     *
     * @param value
     * @return An existing or new child
     */
    public TrieNode AddChild(char value, int rank) {
        TrieNode e = FindChild(value);

        if (e != null)
            return e;

        TrieNode newNode = new TrieNode();

        newNode.value = value;
        newNode.score = rank;
        newNode.parent = this;

        this.children.put(value, newNode);

        return newNode;
    }

    public String GetFullString(boolean with_root) {
        StringBuilder fullString = new StringBuilder();
        TrieNode pointer = this;
        do {
            fullString.insert(0, pointer.value);
            pointer = pointer.parent;
        } while (pointer != null);

        return with_root ? fullString.toString() : fullString.substring(1);
    }

    public boolean IsLeaf() {
        return this.children.size() == 0;
    }

    /**
     * Fix ranks
     *
     * @return largest score among all children
     */
    public int FixRank() {
        if (this.score == 0)
            return 0;

        int dummy = this.score;

        for (TrieNode child : this.children.values()) {
            dummy = Math.max(dummy, child.FixRank());
        }
        this.score = dummy;
        return dummy;
    }

    public String toString() {
        return String.format("%s, %s, Rank %d, Children %d", this.value, GetFullString(true), this.score, this.children.values().size());
    }

    @Override
    public int compareTo(TrieNode to) {
        if (score < to.score)
            return 1;

        if (score > to.score)
            return -1;

        return 0;
    }
}
