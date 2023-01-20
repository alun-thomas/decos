package jpsgcs.alun.jtree;

import java.util.Set;
import java.util.LinkedHashSet;

public class MaxCliquePrior<V> extends SMGraphLaw<V>
{
	public MaxCliquePrior(int maxclique)
	{
		max = maxclique;
	}

	public double logPotential(Set<V> c)
	{
		return c.size() <= max ? 0 : Double.NEGATIVE_INFINITY;
	}

	private int max = 2;
}
