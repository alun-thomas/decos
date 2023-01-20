package jpsgcs.alun.decos;

import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.LinkedList;

import jpsgcs.alun.graph.Network;
import jpsgcs.alun.graph.Graph;

public class DecoGraph<V> extends Network<V,Object>
{
	protected Auxiliary<V> aux = null;

	public DecoGraph(Collection<V> verts, int opt)
	{
		super(false,false,false);

		for (V v : verts)
			add(v);

		switch (opt)
		{
		case 0: aux = null;
			break;
		case 1: aux = new JunctionTree<V>(verts);
			break;
		case 2: aux = new AlmondTree<V>(verts);
			break;
		case 3: aux = new IbarraGraph<V>(verts);
			break;
		default:
			System.err.println("Warning: option "+opt+" not known. Using 0");
			aux = null;
			break;
		}
	}

	public Graph<Set<V>,Set<V>> getAuxiliaryGraph()
	{
		//return aux == null ? this : aux.getGraph();
		return aux == null ? null : aux.getGraph();
	}

	public Set<V> commonNeighbours(V x, V y)
	{
		Set<V> S = new LinkedHashSet<V>(getNeighbours(x));
		S.retainAll(getNeighbours(y));
		return S;
	}

	public boolean flip(V x, V y)
	{
		if (connects(x,y))
			return disconnect(x,y);
		else
			return connect(x,y);
	}

	public boolean connect(V x, V y)
	{
		if (connects(x,y))
			return false;

		Set<V> S = commonNeighbours(x,y);
	
		if (aux == null)
		{
			Set<V> seen = new LinkedHashSet<V>(S);
			Queue<V> q = new LinkedList<V>();

			for (q.add(x), seen.add(x); !q.isEmpty(); )
			{
				V p = q.poll();
	
				if (p == y)
					return false;

				for (V n : getNeighbours(p))
					if (!seen.contains(n))
					{
						seen.add(n);
						q.add(n);
					}
			}

			return super.connect(x,y);
		}

/*
		if (!S.equals(aux.commonNeighbours(x,y)))
		{
			System.err.println("Common neighbour error");
			System.err.println(connects(x,y));
			System.err.println(x+" "+getNeighbours(x));
			System.err.println(y+" "+getNeighbours(y));
			System.err.println(S);
			System.err.println(aux.commonNeighbours((V)x,(V)y));
			System.exit(1);
		}
*/

		if (aux.connect(x,y,S))
			return super.connect(x,y);

		return false;
	}

	public boolean disconnect(Object x, Object y)
	{
		if (!connects(x,y))
			return false;
		
		Set<V> S = new LinkedHashSet<V>(commonNeighbours((V)x,(V)y));

		if (aux == null)
		{
			for (V u : S)
				for (V v : S)
					if (u != v && !connects(u,v))
						return false;
			return super.disconnect(x,y);
		}

/*
		if (!S.equals(aux.commonNeighbours((V)x,(V)y)))
		{
			System.err.println("Common neighbour error");
			System.err.println(connects(x,y));
			System.err.println(x+" "+getNeighbours(x));
			System.err.println(y+" "+getNeighbours(y));
			System.err.println(S);
			System.err.println(aux.commonNeighbours((V)x,(V)y));
			System.exit(1);
		}
*/

		if (aux.disconnect((V)x,(V)y,S))
			return super.disconnect(x,y);

		return false;
	}
}
