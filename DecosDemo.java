
import jpsgcs.alun.decos.DecoGraph;
import jpsgcs.alun.util.ArgParser;
import jpsgcs.alun.hashing.RandomSet;
import jpsgcs.alun.viewgraph.GraphFrame;
import jpsgcs.alun.graph.DAGLocator;
import jpsgcs.alun.graph.Graph;
import jpsgcs.alun.util.Monitor;
import jpsgcs.alun.markov.Parameter;
import jpsgcs.alun.util.InputFormatter;
import jpsgcs.alun.jtree.WSMGraphLaw;
import jpsgcs.alun.jtree.ProductGraphLaw;
import jpsgcs.alun.jtree.MaxCliquePrior;
import jpsgcs.alun.jtree.EdgePenaltyPrior;

import java.util.Random;
import java.util.Set;
import java.util.LinkedHashSet;
import java.awt.Dimension;

public class DecosDemo
{
	public static void main(String[] args)
	{
		try
		{
		/* Initialize resource monitor. */

			Monitor m = new Monitor();

		/* Handle command line parameters. */

			ArgParser ap = new ArgParser(args);

			int n = ap.intAfter("-n",20);
			int its = ap.intAfter("-i",1000000);
			int interval = ap.intAfter("-r",10000);
			int seed = ap.intAfter("-s",0);
			double alpha = ap.doubleAfter("-a",0.0);
			int max = ap.intAfter("-m",n);
			boolean terse = ap.gotOpt("-v");
			boolean stepping = ap.gotOpt("-t");
			boolean help = ap.gotOpt("-h");
			int opt = ap.intAfter("-o",-1);

			if (help)
			{
				System.err.println("Usage: java DecosDemo [options]");
				System.err.print("Options:\t");
				System.err.println("-h\t : Print this message.");
				System.err.println("\t\t-n k\t : Set the size of the graph to int k. Default is 20.");
				System.err.println("\t\t-i k\t : Set the number of iterations to int k. Default is 1000000.");
				System.err.println("\t\t-r k\t : Set the reporting interval to int k. Default is every 10000 iterations.");
				System.err.println("\t\t-s k\t : Set the random seed to int k. Default is pseudo random seed.");

				System.err.println("\t\t-m k\t : Set the maximum clique size to int k. Default is no limit.");
				System.err.println("\t\t-a x\t : Set the edge penalty in the prior to double x. Default is 0.");
				System.err.println("\t\t-v\t : Set output options to terse. Default is verbose.");
				System.err.println("\t\t-t\t : Set sampler to wait for user to hit return before each iteration is continued.");
				System.err.println("\t\t\t   Default is not to wait.");
				System.err.println("\t\t-o k\t : Set the graph representation to the one indexed by int k and run without GUI.");
				System.err.println("\t\t\t   Default is to run all representations in GUI.");
				System.err.println("\t\t\t   The options are:");
				System.err.println("\t\t\t\t 0 = The decomosable graph itself.");
				System.err.println("\t\t\t\t 1 = Junction tree.");
				System.err.println("\t\t\t\t 2 = Almond tree.");
				System.err.println("\t\t\t\t 3 = Ibarra graph.");

				System.exit(0);
			}


		/* Set random seed, make graph vertices, and define sampling distribution. */ 

			Random rand = (seed > 0 ? new Random(seed) : new Random());

			RandomSet<Integer> bag = makeBag(n,rand);

			WSMGraphLaw<Integer> law = new ProductGraphLaw<Integer>(new MaxCliquePrior<Integer>(max),new EdgePenaltyPrior<Integer>(alpha));


		/* If a graph representation is specified, run the sampler using that, output results, and quit. */

			if (opt >= 0)
			{
				DecoGraph<Integer> g = new DecoGraph<Integer>(bag,opt);

				int nedge = 0;
				double logacceptance = 0;
				Integer x = null;
				Integer y = null;

				for (int i=1; i<=its; i++)
				{
					x = bag.draw();
					y = bag.sample();
					bag.add(x);

					logacceptance = logAcceptanceConnect(g,law,x,y);
	
					if (g.connects(x,y))
					{
						if (Math.log(rand.nextDouble()) < -logacceptance && g.disconnect(x,y))
							nedge--;
					}
					else
					{
						if (Math.log(rand.nextDouble()) < logacceptance && g.connect(x,y))
							nedge++;
					}
	
					report(!terse && i % interval == 0,i+"\t"+nedge+"\n");
				}

				report(terse,opt+"\t"+its+"\t"+nedge+"\t"+m.time()+"\n");
				report(!terse,"Iterations = "+its+"\t number of edges = "+nedge+"\nTime = "+m.time()+"\n");

				System.exit(0);
			}

		/* Otherwise, run graphical demo showing sampling with all representations. */
		/* Use graph itself primarily, but check for consistency with other representations. */

			DecoGraph<Integer> g = new DecoGraph<Integer>(bag,0);
			Parameter pause = new Parameter("Wait",0,1000,1000);
			//GraphFrame fg = new GraphFrame(g.getAuxiliaryGraph(),asArray(pause));
			GraphFrame<Integer,Object> fg = new GraphFrame<Integer,Object>(g,asArray(pause));
			fg.setTitle("Decompsable Graph");
			resize(fg,800,500);

			DecoGraph<Integer> jt = new DecoGraph<Integer>(bag,1);
			GraphFrame<Set<Integer>,Set<Integer>> fjt = new GraphFrame<Set<Integer>,Set<Integer>>(jt.getAuxiliaryGraph());
			fjt.setTitle("Junction Tree");
			resize(fjt,800,500);

			DecoGraph<Integer> at = new DecoGraph<Integer>(bag,2);
			GraphFrame<Set<Integer>,Set<Integer>> fat = new GraphFrame<Set<Integer>,Set<Integer>>(at.getAuxiliaryGraph(),
				new DAGLocator<Set<Integer>,Set<Integer>>());
			fat.setTitle("Almond Tree");
			resize(fat,800,500);

			DecoGraph<Integer> ig = new DecoGraph<Integer>(bag,3);
			GraphFrame<Set<Integer>,Set<Integer>> fig = new GraphFrame<Set<Integer>,Set<Integer>>(ig.getAuxiliaryGraph(),
				new DAGLocator<Set<Integer>,Set<Integer>>());
			fig.setTitle("Ibarra Graph");
			resize(fig,800,500);

			int nedge = 0;

			for (int i=its, j=0; i!=0; i--, j++)
			{
				Integer x = bag.draw();
				Integer y = bag.draw();
				bag.add(x);
				bag.add(y);
				
				String move = null;
				double logacceptance = 0;
				int effect = 0;
				if (g.connects(x,y))
				{
					move = "disconnect";
					logacceptance = -logAcceptanceConnect(g,law,x,y);
					effect = -1;
				}
				else
				{
					move = "connect";
					logacceptance = logAcceptanceConnect(g,law,x,y);
					effect = 1;
				}

				report(stepping,j+"::\t"+x+"\t"+y+"\t"+"Try "+move+"... ");

				if (stepping)
					System.in.read();
				else
					Thread.sleep((int)(pause.getValue()));

				if (Math.log(rand.nextDouble()) > logacceptance)
				{
					report(stepping,"Reject.\n");
				}
				else
				{
					boolean legal = g.flip(x,y);

					if (legal)
					{
						report(stepping,"Accept and legal.\n");
						nedge += effect;
					}
					else
					{
						report(stepping,"Accept but NOT legal.\n");
					}
				
					if (legal != jt.flip(x,y))
						System.err.println("Junction tree "+move+" error.");
					if (legal != at.flip(x,y))
						System.err.println("Almond tree "+move+" error.");
					if (legal != ig.flip(x,y))
						System.err.println("Ibarra graph "+move+" error.");
				}
			}

			System.err.println("Iterations = \t"+its+"\t Edges = "+nedge);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public static double logAcceptanceConnect(Graph<Integer,Object> g, WSMGraphLaw<Integer> law, int x, int y)
	{
		Set<Integer> s = new LinkedHashSet<Integer>(g.getNeighbours(x));
		s.retainAll(g.getNeighbours(y));

		double logacc = law.logPotential(s,true);
		s.add(x);
		logacc -= law.logPotential(s,true);
		s.add(y);
		logacc += law.logPotential(s,true);
		s.remove(x);
		logacc -= law.logPotential(s,true);

		return logacc;
	}

	public static void report(boolean doit, String s)
	{
		if (doit)
			System.err.print(s);
	}

	public static void resize(GraphFrame f, int w, int h)
	{
		f.getCanvas().setSize(w,h);
		f.pack();
		Dimension d = f.getCanvas().getSize();
		f.getCanvas().getTransform().setToIdentity();
		f.getCanvas().getTransform().translate(d.width/2,d.height/2);
	}

	public static RandomSet<Integer> makeBag(int n, Random rand)
	{
		RandomSet<Integer> bag = new RandomSet<Integer>(rand);
		for (int i=0; i<n; i++)
		{
			Integer t = i;
			bag.add(t);
		}
		return bag;
	}

	public static Parameter[] asArray(Parameter x)
	{
		Parameter[] a = new Parameter[1];
		a[0] = x;
		return a;
	}
}
