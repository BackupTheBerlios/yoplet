package org.lostinthegarden.applet.graphic;

public interface Outputable {
  static final char CR = '\n';

  // A method to print a string
  public void print(String str);

  // A method to print a string with a carriage return
  public void println(String str);

}