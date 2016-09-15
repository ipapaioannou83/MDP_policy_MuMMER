package MuMMER;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.Formatter;

/**
 * Created by elveleg on 9/9/2016.
 */


public class MDP_trainer {
    public static final double discount = 0.99;
    public static final double learning_rate = 0.5;
    public static final double lamda = 0.3;
    public static final double epsilon = 0.99;
    private EpsilonGreedy learnedPolicy = new EpsilonGreedy(epsilon);
    private SADomain domain;


    public MDP_trainer(){
        mDomain world = new mDomain();
        domain = world.generateDomain();
        State initialState = mState.getInitialState();
        SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);

        SarsaLam la = new SarsaLam( domain, discount, new SimpleHashableStateFactory(), 0, learning_rate, lamda);

        la.setLearningPolicy(learnedPolicy);
        learnedPolicy.setSolver(la);

        for (int i = 0; i < 70000; i++) {
            learnedPolicy.setEpsilon(epsilon / (i + 1));
            la.setLearningPolicy(learnedPolicy);
            learnedPolicy.setSolver(la);

            la.runLearningEpisode(env);

            env.resetEnvironment();
        }


        //Export policy to file
        //la.writeQTable("exportedPolicy.txt");

        generatePlots(domain, env);
    }

    public String printStateTransision (State state, String action){
        String result = "";
        Formatter formatter = new Formatter();
        mState s = (mState) state;
        result = formatter.format("%8s %5s %10s %7s %7s %9s %11s %7s %6s %5s %8s %7s %10s %12s",
                String.valueOf(s.distance),
                String.valueOf(s.mode),
                String.valueOf(s.usrEngChat),
                String.valueOf(s.timeout),
                String.valueOf(s.lowConf),
                String.valueOf(s.tskFilled),
                String.valueOf(s.ctxTask),
                String.valueOf(s.tskCompleted),
                String.valueOf(s.usrEngaged),
                String.valueOf(s.bye),
                String.valueOf(s.usrTermination),
                String.valueOf(s.turnTaking),
                String.valueOf(action),
                String.valueOf(s.prevAct)) + "\n";

        return result;
    }

    public static void generatePlots(Domain domain, SimulatedEnvironment env){

        LearningAgentFactory sarsaLearningFactory = new LearningAgentFactory() {
            @Override
            public String getAgentName() {
                return "SARSA";
            }

            @Override
            public LearningAgent generateAgent() {
                return new SarsaLam((SADomain) domain, discount, new SimpleHashableStateFactory(), 0.0, learning_rate, lamda);
            }
        };


        LearningAlgorithmExperimenter lAlgorithm = new LearningAlgorithmExperimenter(env, 100, 1500, sarsaLearningFactory);
        lAlgorithm.setUpPlottingConfiguration(800, 300, 2, 1000,
                TrialMode.MOST_RECENT_AND_AVERAGE,
                PerformanceMetric.CUMULATIVE_STEPS_PER_EPISODE,
                PerformanceMetric.AVERAGE_EPISODE_REWARD);
        lAlgorithm.startExperiment();
        //lAlgorithm.writeStepAndEpisodeDataToCSV("expData");
    }

    public static void main(String [] args){
        new MDP_trainer();
    }
}