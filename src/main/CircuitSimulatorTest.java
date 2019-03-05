package main;

import flanagan.complex.Complex;
import java.util.ArrayList;
import flanagan.math.Matrix;
import main.Component.ComponentType;

public class CircuitSimulatorTest {
    public static void main(String[] args) {
        Component vs = new Component(ComponentType.INDEPENDENT_VOLTAGE_SOURCE, "Vs", 1, 0, 10);
        Component r1 = new Component(ComponentType.RESISTOR, "R1", 1, 0, 1000);
        Component r2 = new Component(ComponentType.RESISTOR, "R2", 2, 0, 500);
        Component l1 = new Component(ComponentType.INDUCTOR, "L1", 2, 3, 0.0000001);
        Component c1 = new Component(ComponentType.CAPACITOR, "C1", 1, 2, 0.0000005);
        Component i1 = new Component(ComponentType.INDEPENDENT_CURRENT_SOURCE, "I1", 2, 0, 0.003);
        Component i2 = new Component(ComponentType.INDEPENDENT_CURRENT_SOURCE, "I2", 3, 0, 0.007);

        ArrayList<Component> circuit_list = new ArrayList<>();
        circuit_list.add(vs);
        circuit_list.add(r1);
        circuit_list.add(r2);
        circuit_list.add(l1);
        circuit_list.add(c1);
        circuit_list.add(i1);
        circuit_list.add(i2);

        Circuit circuit = new Circuit(circuit_list);

        double[] dc = circuit.do_DC_simulation();

        double frequency_start = 0;
        double frequency_range = 1000000000;
        int num_frequency_steps = 1000;
        boolean doLog = true;

        ACResult[] acResults = circuit.do_AC_simulation(frequency_start, frequency_range, num_frequency_steps, doLog);

        System.out.println("---------------------------------------------");
        System.out.println("G:");
        printMatrix(circuit.G);
        System.out.println("---------------------------------------------");
        System.out.println("C:");
        printMatrix(circuit.C);
        System.out.println("---------------------------------------------");
        System.out.println("b:");
        printVector(circuit.b);
        System.out.println("---------------------------------------------");
        System.out.println("DC Solution:");
        printVector(dc);

        for (ACResult acResult : acResults) {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("AC Solution @" + acResult.frequency + "Hz:");
            printComplexVector(acResult.vector);
        }
    }

    private static void printComplexVector(Complex[] v) {
        System.out.println();
        for (Complex complex : v) {
            System.out.println(complex);
        }
        System.out.println();
    }

    private static void printVector(double[] v) {
        System.out.println();
        for (double n : v) {
            System.out.println(n);
        }
        System.out.println();
    }

    private static void printMatrix(Matrix M) {
        int maxWidth = 0;
        System.out.println();
        for(int i = 0; i < M.getNrow(); i++) {
            for(int j = 0; j < M.getNcol(); j++) {
                maxWidth = Math.max(maxWidth, Double.toString(M.getElement(i, j)).length());
            }
        }

        for(int i = 0; i < M.getNrow(); i++) {
            for(int j = 0; j < M.getNcol(); j++) {
                System.out.print(M.getElement(i, j));

                for(int k = Double.toString(M.getElement(i, j)).length(); k <= maxWidth; k++) {
                    System.out.print(" ");
                }
            }

            System.out.println();
        }
        System.out.println();
    }
}
