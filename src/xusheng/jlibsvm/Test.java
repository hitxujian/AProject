package xusheng.jlibsvm;

import edu.berkeley.compbio.jlibsvm.*;
import edu.berkeley.compbio.jlibsvm.kernel.GaussianRBFKernel;
import edu.berkeley.compbio.jlibsvm.kernel.KernelFunction;
import edu.berkeley.compbio.jlibsvm.kernel.PolynomialKernel;
import edu.berkeley.compbio.jlibsvm.oneclass.OneClassSVC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;

public class Test {

    /* svm_type */
    static final int C_SVC = 0;
    static final int NU_SVC = 1;
    static final int ONE_CLASS = 2;
    static final int EPSILON_SVR = 3;
    static final int NU_SVR = 4;

    /* kernel_type */
    static final int LINEAR = 0;
    static final int POLY = 1;
    static final int RBF = 2;
    static final int SIGMOID = 3;
    static final int PRECOMPUTED = 4;
    //KernelFunction kernel;
    SVM svm;

    ImmutableSvmParameter param;

    private MutableSvmProblem problem;		// set by read_problem
    private SolutionModel model;
    private String input_file_name;		// set by parse_command_line
    private String model_file_name;		// set by parse_command_line

    private static final Float UNSPECIFIED_GAMMA = -1F;

    private String inFile = "";
    private String outFile = "";
    private String modelFile = "";

    public static void main(String[] args) throws Exception {

        Test test = new Test();
        test.run();
    }

    private void run() throws Exception {
        ImmutableSvmParameterGrid.Builder builder = ImmutableSvmParameterGrid.builder();
        // default values
		/*	param.svm_type = svm_parameter.C_SVC;
							param.kernel_type = svm_parameter.RBF;
							param.degree = 3;
							param.gamma = 0;
							param.coef0 = 0;*/
        builder.nu = 0.5f;
        builder.cache_size = 100;
        builder.eps = 1e-3f;
        builder.p = 0.1f;
        builder.shrinking = true;
        builder.probability = false;
        builder.redistributeUnbalancedC = true;
        //param.nr_weight = 0;
        //param.weightLabel = new int[0];
        //param.weight = new float[0];

        //default
        String scalingType = null;
        int scalingExamples = 1000;
        boolean normalizeL2 = false;
        int svm_type = 0;
        int kernel_type = 2;
        int degree = 3;
        float gamma = 0;
        float coef0 = 0;

        builder.kernelSet = new HashSet<KernelFunction>();
        builder.kernelSet.add(new GaussianRBFKernel(gamma));
        //builder.kernelSet.add(new PolynomialKernel(degree, gamma, coef0));
        this.param = builder.build();
        svm = new OneClassSVC<>();

        BufferedReader br = new BufferedReader(new FileReader(inFile));
    }
}
