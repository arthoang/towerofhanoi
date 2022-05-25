package model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class Peg {

   private String pegId;
   private Deque<Disc> discs;

   public Peg(String pegId, Deque<Disc> discs) {
      this.pegId = pegId;
      this.discs = discs;
   }

   public Peg(String pegId) {
      this.pegId = pegId;
      this.discs = new ArrayDeque<>();
   }

   public Disc popDisc() {
      return discs.pop();
   }

   public Disc peekDisc() { return discs.peek(); }

   public boolean pushDisc(Disc d) {
      if (!discs.isEmpty()) {
         if (discs.peek().getDiscSize() > d.getDiscSize()) {
            discs.push(d);
            return true;
         }
      } else {
         discs.push(d);
         return true;
      }
      return false;
   }

   public boolean existDisc(String discId) {
      return this.discs.stream().anyMatch(d -> d.getDiscId().equals(discId));
   }

   public String getPegId() {
      return pegId;
   }

   public void setPegId(String pegId) {
      this.pegId = pegId;
   }

   public int getNumberOfDiscs() {
      return this.discs.size();
   }

   public Disc[] getDiscsArray() {
      return this.discs.toArray(new Disc[this.discs.size()]);
   }

   public Iterator<Disc> getDiscIterator() {
      return this.discs.iterator();
   }

   public Iterator<Disc> getReverseDiscIterator() {
      return this.discs.descendingIterator();
   }

   @Override
   public String toString() {
      return getPegId() + ": " + discs;
   }
}
