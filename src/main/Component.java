package main;

/* Represents circuit components of several types with their value and connections */
public class Component {
    public String name;
    public int nodeIn;
    public int nodeOut;
    public ComponentType type;
    public double value;

    public Component(ComponentType type, String name, int nodeIn, int nodeOut, double value) {
        this.type = type;
        this.name = name;
        this.nodeIn = nodeIn;
        this.nodeOut = nodeOut;
        this.value = value;
    }

    enum ComponentType {INDEPENDENT_VOLTAGE_SOURCE, INDEPENDENT_CURRENT_SOURCE, RESISTOR, CAPACITOR, INDUCTOR}
}
