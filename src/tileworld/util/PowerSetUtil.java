/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Xu Meng
 */
public class PowerSetUtil {

    private static <T> void getSubsets(List<T> superSet, int k, int idx, Set<T> current, List<Set<T>> solution) {
        //successful stop clause
        if (current.size() == k) {
            solution.add(new HashSet(current));
            return;
        }
        //unseccessful stop clause
        if (idx == superSet.size()) {
            return;
        }
        T x = superSet.get(idx);
        current.add(x);
        //"guess" x is in the subset
        getSubsets(superSet, k, idx + 1, current, solution);
        current.remove(x);
        //"guess" x is not in the subset
        getSubsets(superSet, k, idx + 1, current, solution);
    }

    public static <T> List<Set<T>> getSubsets(List<T> superSet, int k) {
        List<Set<T>> res = new ArrayList();
        getSubsets(superSet, k, 0, new HashSet<T>(), res);
        return res;
    }
}
