package lumien.perfectspawn.Transformer;

import java.util.Map;

import lumien.perfectspawn.PerfectSpawn;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

//@MCVersion("1.7.10")
@TransformerExclusions({"lumien.perfectspawn.Transformer.TransformUtils"})
@IFMLLoadingPlugin.SortingIndex(1001)
public class PSLoadingPlugin implements IFMLLoadingPlugin
{
	public static boolean IN_MCP;
	
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { PSClassTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		IN_MCP = !(Boolean)data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
