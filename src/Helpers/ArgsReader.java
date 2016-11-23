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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

public class ArgsReader {

    private HashMap<String, List<String>> arg_list = new HashMap<>();

    public ArgsReader(String[] args) {
        Parse(args);
    }

    private void Parse(String[] args) {
        int i = 0;

        String key = "";

        while (i < args.length) {
            String crt = args[i];

            if (crt.charAt(0) == '-') {
                key = crt.substring(1, crt.length());
            } else {
                if (arg_list.containsKey(key))
                    arg_list.get(key).add(crt);
                else {
                    ArrayList<String> e = new ArrayList<>();
                    e.add(crt);

                    arg_list.put(key, e);
                }
            }

            i++;
        }
    }

    public String Get(String key, int i, String def) {
        try {
            return arg_list.get(key).get(i);
        } catch (Exception e) {
            return def;
        }
    }
}
