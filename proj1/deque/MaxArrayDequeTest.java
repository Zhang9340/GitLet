package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class MaxArrayDequeTest {
    public static class myComparator1<Integer> implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return (java.lang.Integer) o1 - (java.lang.Integer)o2;
        }
    }

    public static class myComparator2<String> implements Comparator<String>{

        @Override
        public int compare(String str1, String str2) {
            return str1.toString().compareTo(str2.toString());
        }
    }

    @Test

    public void Max_test(){
        myComparator1<Integer> c1=new myComparator1<>();
        myComparator2<Integer>c2 =new myComparator2<>();
        MaxArrayDeque<Integer> a =new MaxArrayDeque<>(c1);
        for (int i = 0; i <10 ; i++) {
            a.addFirst(i);
        }
        assertEquals(9,a.max(),0);
        assertEquals( a.max(c2), a.max(),0);

    }
}
