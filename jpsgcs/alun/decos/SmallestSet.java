package jpsgcs.alun.decos;

import java.util.Set;
import java.util.Comparator;

public class SmallestSet implements Comparator<Set>
{
	public int compare(Set x, Set y)
	{
		return ( x.size() < y.size() ? -1 : ( x.size() > y.size() ? 1 : 0 ) );
	}

	public boolean equals(Object obj)
	{
		return obj == this;
	}
}
