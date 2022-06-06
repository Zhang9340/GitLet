package deque;

import java.util.Iterator;

public class ArrayDeque<T>implements Deque<T>,Iterable<T>{
    private int size;
    private T[] items;
    private int nextFirst;
    private int nextLast;


    public ArrayDeque(){
        items= (T[])new Object[8];
        size=0;
        nextFirst=0;
        nextLast= 0;

    }
    @Override
    public int size(){
        return size;
    }

    @Override
    public void printDeque() {
     if (isEmpty()){
         System.out.println();
     }else {
         for (int i=0; i<size-1;i++){
             System.out.print(get(i)+"");
         }
         System.out.println();
     }

    }
    @Override
    public void addFirst(T x){
        if (x==null){
            throw new IllegalArgumentException("cannot be null");
        }
        if(isEmpty()){
          items[nextFirst]=x;
          nextFirst=(nextFirst+ items.length-1)%items.length;
          nextLast=(nextLast+1)%items.length;
          size+=1;
        }
        else if (items[nextFirst]==null){
            items[nextFirst]=x;
            nextFirst=(nextFirst+ items.length-1)%items.length;
            size+=1;
        }else {
            resize(size*2);
            items[nextFirst]=x;
            nextFirst=(nextFirst+items.length-1)%items.length;

            size+=1;
        }
    }
    @Override
    public void addLast(T x){
        if (x==null){
            throw new IllegalArgumentException("cannot be null");
        }
        if(isEmpty()){
            items[nextLast]=x;
            nextLast=(nextLast+1)%items.length;
            nextFirst=(nextFirst+items.length-1)%items.length;
            size+=1;
        }
        else if (items[nextLast]==null){
            items[nextLast]=x;
            nextLast=(nextLast+1)%items.length;
            size+=1;
        }else {
            resize(size*2);
            items[nextLast]=x;
            nextLast=(nextLast+1)%items.length;
            size+=1;
        }
    }
    @Override
    public T removeFirst(){
        if (isEmpty()){
            return null;
        }
        if (size==1){
            T removedItem= get(0);
            items[(nextFirst+1)% items.length]= null;
            nextLast=(nextLast-1+items.length)%items.length;
            nextFirst=(nextFirst+1)%items.length;
            size-=1;

            return removedItem;
        }
        T returnItem= get(0);
        items[(nextFirst+1)% items.length]=null;
        size-=1;
        nextFirst=(nextFirst+1)% items.length;
        double usage=size/(double)items.length;
        if(items.length>=16&&usage<0.25){
            resize(items.length/2);
        }
        return  returnItem;


    }
    @Override
    public T removeLast(){

        if (isEmpty()){
            return null;
        }
        if (size==1){
            T removedItem= get(size-1);
            items[(nextLast+ items.length-1)% items.length]= null;
            nextLast=(nextLast+items.length-1)%items.length;
            nextFirst=(nextFirst+1)%items.length;
            size-=1;

            return removedItem;
        }
          T returnItem= get(size-1);
         items[(nextFirst+size)% items.length]=null;
         size-=1;
         nextLast=(nextLast+items.length-1)%items.length;
         double usage=size/(double)items.length;
        if(items.length>=16&&usage<0.25){
             resize(items.length/2);
         }
         return  returnItem;

    }
    @Override
    public T get(int index){
        if (isEmpty()){
            return null;
        }
        if(index>size-1||index<0){
           return null;
       }

       int realIndex=(nextFirst+index+1)% items.length;


     return items[realIndex];
    }

    private  void  resize(int capacity){
        T[] newArray= (T[]) new Object[capacity];
        for (int i=0; i<size; i++){
            newArray[i]=get(i);
        }
        items=newArray;
        nextFirst=items.length-1;
        nextLast=size;

    }


    @Override
    public Iterator<T> iterator() {
        return new ArrDequeIterator();
    }
    private class ArrDequeIterator implements Iterator<T>{
        private int wizPos;
        @Override
        public boolean hasNext() {
            return wizPos<size;
        }

        @Override
        public T next() {
            T returnItem=get(wizPos);
            wizPos+=1;
            return returnItem;
        }
    }

    @Override
    public boolean equals(Object o){
        if (o==null){return false;}
        if (this==o){return true;}
        if (!(o instanceof Deque) ||  ((Deque<?>) o).size()!=this.size){return false;}
        Deque<T> object= (Deque<T>) o;
        for (int i = 0; i <size ; i++) {
            if (!this.get(i).equals(object.get(i))){
                return false;
            }
        }
        return true;
    }
}
