/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Xu Meng
 */
public class CombinatoricsUtil {

    public static long C(long total, long choose) {
        if (total < choose) {
            return 0;
        }
        if (choose == 0 || choose == total) {
            return 1;
        }
        return C(total - 1, choose - 1) + C(total - 1, choose);
    }
    
    public static List<List<Integer>> separate(int total, int n){
        List<List<Integer>> result = new ArrayList();
        
        List<Integer> items = new ArrayList();
        for(int i=1;i<=total-1;i++){
            items.add(i);
        }
        
        List<Set<Integer>> subsets = PowerSetUtil.getSubsets(items, n-1);
        for(Set<Integer> subset : subsets){
            int prev = 0;
            List<Integer> seq = new ArrayList();
            for(Integer i : subset){
                seq.add(i-prev);
                prev = i;
            }
            seq.add(total - prev);
            result.add(seq);
        }
        
        return result;
    }
}
