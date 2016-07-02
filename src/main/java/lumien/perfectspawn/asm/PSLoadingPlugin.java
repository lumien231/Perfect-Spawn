package lumien.perfectspawn.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

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
