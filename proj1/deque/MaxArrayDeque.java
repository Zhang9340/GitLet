package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>  {
    private Comparator<T> comparator;
    private ArrayDeque<T> arrayDeque;
    public MaxArrayDeque(Comparator<T> c){
        super();
        this.comparator=c;
    }
    public T max(){
        T maxItem=get(0);
        if (isEmpty()){
            return null;
        }
        for (T item: this
             ) {
            if (comparator.compare(maxItem,item)<0){
                maxItem = item;
            }
            
        }
        return maxItem;
    }

    public T max(Comparator<T> c){
        T maxItem=get(0);
        if (isEmpty()){
            return null;
        }
        for (T item: this
        ) {
            if (c.compare(maxItem,item)<0){
                maxItem = item;
            }

        }
        return maxItem;
    }
}

