package main;

import flanagan.complex.Complex;
import flanagan.complex.ComplexMatrix;
import flanagan.math.Matrix;

import java.util.ArrayList;

public class Circuit {
    public Matrix G;
    public Matrix C;
    public double[] b;

    private ArrayList<Component> components;

    public Circuit(ArrayList<Component> list) {
        this.components = new ArrayList<>(list);

        int num_inductors = 0;
        int num_ind_voltage_sources = 0;
        int num_nodes = 0;

        for(Component element : components) {
            System.out.println(element.name);

            if(element.type == Component.ComponentType.INDEPENDENT_VOLTAGE_SOURCE) num_ind_voltage_sources++;
            if(element.type == Component.ComponentType.INDUCTOR) num_inductors++;

            if(element.nodeIn > num_nodes) num_nodes = element.nodeIn;
            if(element.nodeOut > num_nodes) num_nodes = element.nodeOut;
        }

        int size = num_nodes + num_inductors + num_ind_voltage_sources;

        this.G = new Matrix(size, size, 0);

        this.C = new Matrix(size, size, 0);

        this.b = new double[size];

        int current_voltage_source_offset_offset = num_nodes + 1;
        int current_inductor_offset = num_nodes + num_ind_voltage_sources + 1;

        for(Component element : components) {

            // Resistor Stamp
            if(element.type == Component.ComponentType.RESISTOR) {
                double g = 1 / element.value;
                if(element.nodeIn == 0) {
                    addToMatrix(G, element.nodeOut, element.nodeOut, g);
                } else if (element.nodeOut == 0) {
                    addToMatrix(G, element.nodeIn, element.nodeIn,  g);
                } else {
                    addToMatrix(G, element.nodeIn, element.nodeIn, g);
                    addToMatrix(G, element.nodeOut, element.nodeOut, g);
                    addToMatrix(G, element.nodeOut, element.nodeIn, -g);
                    addToMatrix(G, element.nodeIn, element.nodeOut, -g);
                }
            }

            // Capacitor Stamp
            if(element.type == Component.ComponentType.CAPACITOR) {
                double Cap = element.value;

                if(element.nodeIn == 0) {
                    addToMatrix(C, element.nodeOut, element.nodeOut, Cap);
                } else if (element.nodeOut == 0) {
                    addToMatrix(C, element.nodeIn, element.nodeIn,  Cap);
                } else {
                    addToMatrix(C, element.nodeIn, element.nodeIn, Cap);
                    addToMatrix(C, element.nodeOut, element.nodeOut, Cap);
                    addToMatrix(C, element.nodeOut, element.nodeIn, -Cap);
                    addToMatrix(C, element.nodeIn, element.nodeOut, -Cap);
                }
            }

            // Inductor Stamp
            if(element.type == Component.ComponentType.INDUCTOR) {
                double L = element.value;

                if(element.nodeIn == 0) {
                    addToMatrix(G, element.nodeOut, current_inductor_offset, -1);
                    addToMatrix(G, current_inductor_offset, element.nodeOut, -1);
                } else if (element.nodeOut == 0) {
                    addToMatrix(G, element.nodeIn, current_inductor_offset, 1);
                    addToMatrix(G, current_inductor_offset, element.nodeIn, 1);
                } else {
                    addToMatrix(G, element.nodeIn, current_inductor_offset, 1);
                    addToMatrix(G, element.nodeOut, current_inductor_offset, -1);
                    addToMatrix(G, current_inductor_offset, element.nodeIn, 1);
                    addToMatrix(G, current_inductor_offset, element.nodeOut, -1);
                }

                addToMatrix(C, current_inductor_offset, current_inductor_offset, -L);
                current_inductor_offset++;
            }

            // Current Source Stamp
            if(element.type == Component.ComponentType.INDEPENDENT_CURRENT_SOURCE) {
                double I = element.value;
                if(element.nodeIn == 0) {
                    addToVector(b, element.nodeOut, -I);
                } else if (element.nodeOut == 0) {
                    addToVector(b, element.nodeIn, I);
                } else {
                    addToVector(b, element.nodeIn, I);
                    addToVector(b, element.nodeOut, -I);
                }
            }

            // Voltage Source Stamp
            if(element.type == Component.ComponentType.INDEPENDENT_VOLTAGE_SOURCE) {
                double E = element.value;

                if(element.nodeIn == 0) {
                    addToMatrix(G, element.nodeOut, current_voltage_source_offset_offset, -1);
                    addToMatrix(G, current_voltage_source_offset_offset, element.nodeOut, -1);
                } else if (element.nodeOut == 0) {
                    addToMatrix(G, element.nodeIn, current_voltage_source_offset_offset, 1);
                    addToMatrix(G, current_voltage_source_offset_offset, element.nodeIn, 1);
                } else {
                    addToMatrix(G, element.nodeIn, current_voltage_source_offset_offset, 1);
                    addToMatrix(G, element.nodeOut, current_voltage_source_offset_offset, -1);
                    addToMatrix(G, current_voltage_source_offset_offset, element.nodeIn, 1);
                    addToMatrix(G, current_voltage_source_offset_offset, element.nodeOut, -1);
                }

                addToVector(b, current_voltage_source_offset_offset, E);
                current_voltage_source_offset_offset++;
            }
        }
    }

    private static void addToVector(double[] b, int i, double k) {
        b[i-1] += k;
    }

    private static void addToMatrix(Matrix M, int i, int j, double k) {
        M.setElement(i - 1, j - 1, M.getElement(i - 1, j - 1) + k);
    }

    public double[] do_DC_simulation() {
        return this.G.solveLinearSet(this.b);
    }

    public ACResult[] do_AC_simulation(double frequency_start, double frequency_range, int num_frequency_steps, boolean doLog) {
        ACResult[] acResults = new ACResult[num_frequency_steps + 1];

        int size = this.G.getNcol();

        for(int step = 0; step <= num_frequency_steps; step++) {
            double f = frequency_start;

            if(doLog) {
                if(step != 0) { // Special case for log so that we evaluate the start frequency as well.
                    f += Math.pow(10, (step - 1) * Math.log10(frequency_range)/(num_frequency_steps - 1));
                }
            } else {
                f += step * frequency_range / num_frequency_steps;
            }

            double omega = 2 * Math.PI * f;

            ComplexMatrix A = new ComplexMatrix(size, size);
            Complex[] B = new Complex[size];

            for(int i = 0; i < size; i++) {
                for(int j = 0; j < size; j++) {
                    A.setElement(i, j, this.G.getElement(i, j), this.C.getElement(i, j) * omega);
                }

                B[i] = new Complex(this.b[i], 0.0);
            }

            acResults[step] = new ACResult(f, A.solveLinearSet(B));
        }

        return acResults;
    }
}
