package com.kingbrezz.tickscope.i18n;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.ChatColor;
import java.util.HashMap;
import java.util.Map;

public final class MessageManager {
    private final TickScope plugin;
    private final Map<String,String> messages = new HashMap<>();
    public MessageManager(TickScope plugin){ this.plugin=plugin; reload(); }
    public void reload(){
        messages.clear();
        String lang=plugin.getConfig().getString("language.default","id").toLowerCase();
        if(!plugin.getConfig().getStringList("language.available").contains(lang)) lang="id";
        String resource="messages_"+lang+".yml";
        java.io.InputStream in=plugin.getResource(resource);
        if(in==null) in=plugin.getResource("messages_id.yml");
        if(in==null) return;
        try(java.io.InputStream input=in){
            org.bukkit.configuration.file.YamlConfiguration y=org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8));
            for(String key:y.getKeys(false)){ String v=y.getString(key); if(v!=null) messages.put(key,v); }
        }catch(java.io.IOException ignored){}
    }
    public String get(String key,String fallback){ return messages.getOrDefault(key,fallback); }
    public String format(String key,String fallback,Map<String,String> vars){
        String s=get(key,fallback);
        for(var e:vars.entrySet()) s=s.replace("{"+e.getKey()+"}",e.getValue());
        return ChatColor.translateAlternateColorCodes('&',s);
    }
}

