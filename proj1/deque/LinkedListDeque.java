package deque;


import java.util.Iterator;

public class LinkedListDeque<T>implements Deque<T>,Iterable<T> {

    private static class node<T>{
        public  T item;
        public  node<T> next;
        public node<T>  prev;
        public  node(node<T> p,T i, node<T> n){
            this.prev=p;
            this.item=i;
            this.next=n;

        }
    }

    private   node<T> sentinel;
    private int size;

    /* create empty list */

    public LinkedListDeque(){
        sentinel= new node<>(null, null , null);
        sentinel.next= sentinel;
        sentinel.prev=  sentinel;

        size=0;
    }
 @Override
    public void addFirst(T x){
     if (x==null){
         throw new IllegalArgumentException("cannot be null");
     }
        sentinel.next=new node<>(sentinel,x,sentinel.next);
        sentinel.next.next.prev=sentinel.next;
        size+=1;
    }
    @Override
    public void addLast(T x){
        if (x==null){
            throw new IllegalArgumentException("cannot be null");
        }
        sentinel.prev=new node<>(sentinel.prev,x,sentinel);
        sentinel.prev.prev.next= sentinel.prev;
        size+=1;
    }
    @Override
    public T removeFirst(){
        if (isEmpty()){
            return null;
        }
        if(size == 1 ) {
            node<T> removedNode= sentinel.next;
            sentinel.next=sentinel;
            sentinel.prev=sentinel;
        size-=1;
        return removedNode.item;
        }else {
            node<T> removedNode= sentinel.next;
            sentinel.next.next.prev=sentinel;
            sentinel.next=sentinel.next.next;
            size-=1;
            return removedNode.item;
        }
    }
    @Override
    public T removeLast(){
        if (isEmpty()){
            return null;
        }
        if(size == 1 ) {
            node<T> removedNode= sentinel.next;
            sentinel.next=sentinel;
            sentinel.prev=sentinel;
            size-=1;
            return removedNode.item;
        }else {
            node<T> removedNode= sentinel.prev;
            sentinel.prev.prev.next=sentinel;
            sentinel.prev=sentinel.prev.prev;
            size-=1;
            return removedNode.item;
        }
    }

    @Override
    public int size(){return size;}
    @Override
    public boolean equals(Object o){
        if (o==null){return false;}
        if (this==o){return true;}
        if (!(o instanceof Deque) || ((Deque<?>) o).size()!=this.size){return false;}
        Deque<T> other =(Deque<T>) o;
        for (int i = 0; i < size; i++) {

            if (!other.get(i).equals(this.get(i))){
                return false;
            }
        }
        return true;
    }
    @Override
    public T get(int index){
        if (index >= size || index < 0){
            return null;
        }

        node<T> currentNode = sentinel.next;

        for (int i = 0; i < index; i++) {
                currentNode = currentNode.next;
        }

        return currentNode.item;


    }
    public T getRecursive(int index){

        if (index >= size || index < 0){
            return null;
        }
         sentinel=sentinel.next;
        if (index==0){
            T returnValue= sentinel.item;
            /*recover the Deque*/
            findSentinel();
            return returnValue;
        }
        return getRecursive(index-1);
    }
    /*Help method to retrieve sentinel node*/
    private void findSentinel(){
        while (sentinel.item!=null){
            sentinel=sentinel.next;
        }
    }
    @Override
    public void printDeque() {
        if (isEmpty()){
            System.out.println();
            return ;
        }
        node<T> currNode = sentinel.next;

        for (int i = 0; i < size-1; i++){

            System.out.print(currNode.item.toString() + " ");
            currNode = currNode.next;
        }

    }
    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }
    private class DequeIterator implements Iterator<T>{
        private int wizPos;
        private node<T> currNode=sentinel.next;

        public boolean hasNext() {
            return wizPos<size;
        }

        public T next() {
            T returnItem= currNode.item;
            currNode=currNode.next;
            wizPos+=1;
            if (wizPos==size){
                findSentinel();
            }
            return returnItem;}
    }
}

