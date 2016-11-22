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

import java.util.Comparator;
import java.util.List;

public class BABItem {

    public static Comparator<BABItem> byRatio() {
        return new Comparator<BABItem>() {
            public int compare(BABItem i1, BABItem i2) {
                return Double.compare(i2.getRatio(), i1.getRatio());
            }
        };
    }

    public static Comparator<BABItem> byRatioMin() {
        return new Comparator<BABItem>() {
            public int compare(BABItem i1, BABItem i2) {
                return Double.compare(i2.getRatioMin(), i1.getRatioMin());
            }
        };
    }

    public Rule rule;
    public int value;
    public int weight;
    public int weightMin;

    public double getRatio() {
        return (double) value / (weight + 0.01);
    }

    public double getRatioMin() {
        return (double) value / (weightMin + 0.01);
    }

    public String toString() {
        return String.format("%s", rule);
    }
}