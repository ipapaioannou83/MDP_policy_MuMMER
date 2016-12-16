package MuMMER;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.pomdp.PODomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import com.google.gson.Gson;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

/**
 * Created by elveleg on 9/9/2016.
 */


public class MDP_trainer {
    public static final double discount = 0.99;
    public static final double learning_rate = 0.1;
    public static final double lamda = 0.3;
    public static final double epsilon = 0.99;
    private EpsilonGreedy learnedPolicy = new EpsilonGreedy(epsilon);
    private SADomain domain;
    private PODomain podomain;
    private List<Episode> episodes;
    private static final int learning_iterations = 120000;


    public MDP_trainer(){
        mDomain world = new mDomain();
        domain = world.generateDomain();
        State initialState = mState.getInitialState();
        SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);

        QLearning la = new QLearning(domain, discount, new SimpleHashableStateFactory(), 0.0, learning_rate, 40){
        //SarsaLam la = new SarsaLam(domain, discount, new SimpleHashableStateFactory(), 0, learning_rate, lamda) {

            @Override
            public void writeQTable(String path){
                Map<HashableState, QLearningStateNode> qF;
                qF = this.qFunction;
                Gson gson = new Gson();

                try {
                    FileWriter fw = new FileWriter(path);
                    fw.write(gson.toJson(qF));
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };



        la.setLearningPolicy(learnedPolicy);
        learnedPolicy.setSolver(la);

        episodes = new ArrayList<>(learning_iterations);

        for (int i = 0; i < learning_iterations; i++) {
            learnedPolicy.setEpsilon(epsilon / (i + 1));
            la.setLearningPolicy(learnedPolicy);
            learnedPolicy.setSolver(la);

            episodes.add(la.runLearningEpisode(env));

            env.resetEnvironment();
        }


        //Export policy to file
        la.writeQTable("jsonDump_temp.json");

        //generatePlots(domain, env);

        List<State> ls = bestEpisode().stateSequence;
        List<Action> lac = bestEpisode().actionSequence;
        List<Double> lr = bestEpisode().rewardSequence;
        Formatter formatter = new Formatter();
        System.out.println(formatter.format("%8s %5s %10s %7s %7s %9s %11s %7s %6s %5s %8s %7s %10s %12s %6s","Distance", "Mode", "usrEngChat", "timeout",
                "lowConf", "tskFilled", "ctxTask", "tskCompl", "usrEng", "sBye", "usrTerm", "turnTaking", "|actionTaken", "PrevAct", "|Reward"));
        int i = 0;
        for (State state: ls) {
            mState s = (mState) state;
            formatter = new Formatter();
            System.out.println(formatter.format("%8s %5s %10s %7s %7s %9s %11s %7s %6s %5s %8s %7s %10s %12s %6s",
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
                    String.valueOf(lac.get(i).actionName()),
                    String.valueOf(s.prevAct),
                    lr.get(i).intValue()));

            if (i < lac.size()-1)
                ++i;

        }
    }

    public String printStateTransision (State state, String action){
        String result;
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
                return "Hybrid";
            }

            @Override
            public LearningAgent generateAgent() {
                //return new SarsaLam((SADomain) domain, discount, new SimpleHashableStateFactory(), 0.0, learning_rate, lamda);
                return new QLearning((SADomain) domain, discount, new SimpleHashableStateFactory(), 0.0, learning_rate, 40);
            }
        };


        LearningAlgorithmExperimenter lAlgorithm = new LearningAlgorithmExperimenter(env, 100, 5000, sarsaLearningFactory);
        lAlgorithm.setUpPlottingConfiguration(800, 300, 2, 1000,
                TrialMode.MOST_RECENT_AND_AVERAGE,
                PerformanceMetric.CUMULATIVE_STEPS_PER_EPISODE,
                PerformanceMetric.AVERAGE_EPISODE_REWARD);
        lAlgorithm.startExperiment();
        //lAlgorithm.writeStepAndEpisodeDataToCSV("expData");
    }

    public Episode bestEpisode(){
        Episode ep = new Episode();
        double maxR = 0;
        List<Action> aList;
        Boolean found = false;
        for (Episode e : episodes){
            aList = e.actionSequence;
            for (Action act: aList){
                if (act.actionName().equals("requestShop")){
                    if (e.discountedReturn(discount) > maxR){
                        maxR = e.discountedReturn(discount);
                        ep = e;
                    }
                }
            }
//            learnedPolicy.action();
//            if (e.discountedReturn(discount) > maxR){
//                maxR = e.discountedReturn(discount);
//                ep = e;
//            }
        }

        return ep;
    }

    public static void main(String [] args){
        new MDP_trainer();
    }
}