package jpsgcs.alun.jtree;

import java.util.Set;

public abstract class WSMGraphLaw<V>
{
	abstract public double logPotential(Set<V> c, boolean asclique);

//	public double logPotential(Set<V> c)
//	{
//		return logPotential(c,true);
//	}

	public double logProbability(JTree<V> j)
	{
		double x = 0;

		for (Set<V> c : j.cliques)
			x += logPotential(c,true);

		for (Set<V> s : j.separators)
			x -= logPotential(s,false);

		return x;
	}
}
