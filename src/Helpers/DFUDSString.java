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

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DFUDSString {
    public String Parenthesis;
    public String Labels;
    public ArrayList<ArrayList<Integer>> Links;
    public int[] Ranks;

    private ArrayList<ArrayList<Integer>> link_delta;
    private HashMap<Integer, Integer> runtime_label_map = new HashMap<>();

    public DFUDSString(String parenthesis, String labels, int[] ranks, ArrayList<ArrayList<Integer>> links, ArrayList<ArrayList<Integer>> deltas) {
        Parenthesis = parenthesis;
        Labels = labels;
        Ranks = ranks;
        Links = links;
        link_delta = deltas;
    }

    /**
     * Returns the start position of current block
     *
     * @param pos current position
     * @return position of '('
     */
    private int SelectOpen(int pos) {
        if (pos < 0 || pos > Parenthesis.length())
            return -1;

        for (int i = pos - 1; i >= 0; i--) {
            if (Parenthesis.charAt(i) == ')')
                return i + 1;
        }

        return 0;
    }

    /**
     * Returns the close position of current block
     *
     * @param pos current position
     * @return position of ')'
     */
    public int SelectClose(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return -1;

        if (Parenthesis.charAt(pos) == ')')
            return pos;

        for (int i = pos + 1; i < Parenthesis.length(); i++) {
            if (Parenthesis.charAt(i) == ')')
                return i;
        }

        return Parenthesis.length() - 1;
    }

    /**
     * Return the number of children
     *
     * @param pos position within this block
     * @return the count
     */
    private int CountChildren(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return -1;

        pos = SelectOpen(pos);

        int count = 0;

        while (Parenthesis.charAt(pos) != ')') {
            count++;
            pos++;
        }

        return count;
    }

    /**
     * Get the i-th child
     *
     * @param pos         parent block
     * @param child_index i-th
     * @return start of the child
     */
    private int GetChildIndexAt(int pos, int child_index) {
        if (pos < 0 || pos >= Parenthesis.length())
            return -1;

        if (child_index < 0 || child_index >= CountChildren(pos))
            return -1;

        pos = SelectOpen(pos);
        pos += CountChildren(pos) - (child_index + 1);
        pos = FindMatchingParenthesis(pos, 1);
        return pos + 1;
    }

    /**
     * Get all children
     *
     * @param pos parent block
     * @return starts of all children
     */
    public int[] GetAllChildren(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return new int[]{};

        int count = CountChildren(SelectOpen(pos));

        int[] result = new int[count];

        for (int i = 0; i < count; i++) {
            result[i] = GetChildIndexAt(pos, i);
        }

        return result;
    }

    /**
     * Find a child by label
     *
     * @param pos
     * @param label
     * @return
     */
    public int FindChild(int pos, char label) {
        if (pos < 0 || pos >= Parenthesis.length())
            return -1;

        pos = SelectOpen(pos);

        int count = CountChildren(pos);

        for (int i = 0; i < count; i++) {
            int p = GetChildIndexAt(pos, i);

            if (GetLabel(p) == label)
                return p;
        }

        return -1;
    }

    /**
     * Get parent
     *
     * @param pos child block
     * @return parent
     */
    private int GetParent(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return -1;

        pos = SelectOpen(pos) - 1;

        return SelectOpen(FindMatchingParenthesis(pos, -1));
    }

    /**
     * Get label (single node)
     *
     * @param pos the block
     * @return label
     */
    private char GetLabel(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return 0;

        pos = SelectClose(pos);

        int index = CountParenthesis(pos) - 1;

        return Labels.charAt(index);
    }

    /**
     * Get a role of a node: D, L or B
     *
     * @param pos the block
     */
    public int GetRank(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return 0;

        pos = SelectClose(pos);

        int index = CountParenthesis(pos) - 1;

        return Ranks[index];
    }

    /**
     * Get links with deltas
     *
     * @param pos the block
     * @return List of link blocks with deltas
     */
    public ArrayList<Pair<Integer, Integer>> GetLinks(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return new ArrayList<>();

        pos = SelectClose(pos);

        int index = CountParenthesis(pos) - 1;

        ArrayList<Integer> links = Links.get(index);
        ArrayList<Integer> deltas = link_delta.get(index);

        ArrayList<Pair<Integer, Integer>> result = new ArrayList<>();

        for (int i = 0; i < links.size(); i++) {
            result.add(new Pair<>(links.get(i), deltas.get(i)));
        }

        return result;
    }

    /**
     * Add a link
     *
     * @param from  i-th
     * @param to    i-th
     * @param delta delta
     */
    public void AddLink(int from, int to, int delta) {
        from = SelectClose(from);

        int index_from = CountParenthesis(from) - 1;

        Links.get(index_from).add(to);

        // set delta
        link_delta.get(index_from).add(delta);
    }

    /**
     * Get full string (collection of labels from root, root not included)
     *
     * @param pos the block
     * @return the string
     */
    public String GetFullString(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return "";

        StringBuilder sb = new StringBuilder();

        sb.append(GetLabel(pos));

        pos = GetParent(pos);

        while (pos > 0) {
            sb.insert(0, GetLabel(pos));
            pos = GetParent(pos);
        }

        return sb.toString();
    }

    /**
     * @param str the string
     * @return the position of all chars in the string
     */
    public int[] FindMuchByString(String str) {
        List<Integer> result = new ArrayList<>();

        //result.add(0);

        int pos = 0; // we now at root

        //noinspection Duplicates
        for (int i = 0; i < str.length(); i++) {
            int child = FindChild(pos, str.charAt(i));

            if (child == -1)
                break;
            else {
                result.add(child);
                pos = child;
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }

    /**
     * @param str the string
     * @return the position of all chars in the string
     */
    public int[] FindByString(String str) {
        List<Integer> result = new ArrayList<>();

        int pos = 0; // we now at root
        int[] children = new int[1];

        str = "$" + str;

        //noinspection Duplicates
        for (int i = 0; i < str.length(); i++) {

            boolean found = false;

            for (int c : children) {
                if (GetLabel(c) == str.charAt(i)) {
                    result.add(c);
                    found = true;

                    children = GetAllChildren(c);
                    break;
                }
            }

            if (!found)
                break;
        }

        if (result.size() != str.length())
            return new int[0];

        result.remove(0);
        return result.stream().mapToInt(i -> i).toArray();
    }


    /**
     * Count the occurrence of ')' from the beginning
     *
     * @param pos start from
     * @return count
     */
    private int CountParenthesis(int pos) {
        if (pos < 0 || pos >= Parenthesis.length())
            return -1;

        if (runtime_label_map.isEmpty())
            InitLabelMap();

        return runtime_label_map.get(pos);
    }

    private void InitLabelMap() {
        int count = 0;

        for (int i = 0; i < Parenthesis.length(); i++) {
            char crt = Parenthesis.charAt(i);

            if (crt == ')') {
                count++;
                runtime_label_map.put(i, count);
            }
        }
    }

    /**
     * Find the matching parenthesis
     *
     * @param pos       from
     * @param direction 1=forward, -1=backward
     * @return position of the matching parenthesis
     */
    private int FindMatchingParenthesis(int pos, int direction) {
        if (pos < 0 || pos >= Parenthesis.length())
            return -1;

        if (direction == 0)
            return -1;

        int count = 0;
        try {
            do {
                count += this.Parenthesis.charAt(pos) == '(' ? 1 : -1;
                pos += direction;
            } while (count != 0);

            return pos - direction;
        } catch (Exception e) {
            return -1;
        }
    }
}
