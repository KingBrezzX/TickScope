async function loadSpikes(){
 try{
  const a=await getJson("/api/spikes"); const html=a.slice().reverse().slice(0,50).map(x=>`<article class="row"><div><b>${esc(x.severity)}</b><small>${new Date(x.timestamp).toLocaleString()}</small></div><div class="row-right"><b>${Number(x.mspt).toFixed(1)} ms</b><small>${Number(x.tps).toFixed(2)} TPS • ${x.players} players</small></div></article>`).join("")||'<div class="empty">No spikes recorded.</div>';
  const h=document.getElementById("spikesHome"),l=document.getElementById("spikesList");if(h)h.innerHTML=html;if(l)l.innerHTML=html;
 }catch{for(const id of ["spikesHome","spikesList"]){const e=document.getElementById(id);if(e)e.innerHTML='<div class="empty">Unable to load spike history.</div>'}}
}
loadSpikes();setInterval(loadSpikes,5000);
