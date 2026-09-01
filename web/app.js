const C=window.TICKSCOPE_CONFIG||{}; const API=C.apiBase||""; const TOKEN=C.token||"";
function headers(){return TOKEN?{Authorization:"Bearer "+TOKEN,Accept:"application/json"}:{Accept:"application/json"}}
function esc(v){return String(v??"—").replace(/[&<>"']/g,m=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[m]))}
function set(id,v){const e=document.getElementById(id);if(e)e.textContent=v}
async function getJson(path){const r=await fetch(API+path,{headers:headers()});if(!r.ok)throw Error("HTTP "+r.status);return r.json()}
function updateMetrics(d){
 set("tps",Number(d.tps).toFixed(2));set("mspt",Number(d.mspt).toFixed(1));set("players",d.players);set("chunks",d.loadedChunks);set("entities",d.entities);set("tiles",d.tileEntities);
 const tp=Math.max(0,Math.min(100,Number(d.tps)/20*100)), mp=Math.max(0,Math.min(100,Number(d.mspt)/50*100));
 ["tpsBar","analysisTps"].forEach(id=>{const e=document.getElementById(id);if(e)e.style.width=tp+"%"});
 ["msptBar","analysisMspt"].forEach(id=>{const e=document.getElementById(id);if(e)e.style.width=mp+"%"});
 set("tpsValue",Number(d.tps).toFixed(2));set("msptValue",Number(d.mspt).toFixed(1));
 set("severity",d.mspt>=100||d.tps<15?"CRITICAL":d.mspt>=50||d.tps<18?"WARNING":"HEALTHY");
}
function connectRealtime(){
 if(!API){location.href="connect.html";return}
 const url=API+"/api/stream"+(TOKEN?"?token="+encodeURIComponent(TOKEN):"");
 const s=new EventSource(url);
 s.onopen=()=>{set("connection","Live");document.getElementById("statusDot").className="live"};
 s.onmessage=e=>{try{const d=JSON.parse(e.data);if(d.type==="metrics")updateMetrics(d)}catch{}};
 s.onerror=()=>{set("connection","Reconnecting");document.getElementById("statusDot").className=""};
}
function renderList(id,arr,kind){
 const el=document.getElementById(id);if(!el)return;
 if(!Array.isArray(arr)||!arr.length){el.innerHTML='<div class="empty">No data detected.</div>';return}
 el.innerHTML=arr.slice(0,50).map(x=>{
  const loc=x.world?`${esc(x.world)} • X ${esc(x.x)} Y ${esc(x.y)} Z ${esc(x.z)}`:"";
  const player=x.nearestPlayer??x.player??"No nearby player";
  return `<article class="row"><div><b>${esc(x.type||kind||"Hotspot")}</b><small>${loc}</small></div><div class="row-right"><b>${esc(x.score??x.activity??x.count??"—")}</b><small>${kind==="redstone"?"activity":"score"}${player?" • "+esc(player):""}</small></div></article>`
 }).join("")
}
async function loadHotspots(){try{renderList("hotspotsList",await getJson("/api/hotspots"),"hotspot")}catch{renderList("hotspotsList",[])}}
async function loadRedstone(){try{renderList("redstoneList",await getJson("/api/redstone"),"redstone")}catch{renderList("redstoneList",[])}}
async function loadEntities(){try{renderList("entitiesList",await getJson("/api/entities"),"entity")}catch{renderList("entitiesList",[])}}
async function loadTiles(){try{renderList("tilesList",await getJson("/api/tile-entities"),"tile")}catch{renderList("tilesList",[])}}
async function loadRecommendations(){
 try{const a=await getJson("/api/recommendations");const e=document.getElementById("recommendations");e.innerHTML=a.length?a.map(x=>`<article class="row"><div><b>${esc(x.severity)} • ${esc(x.cause)}</b><small>${esc(x.recommendation)}</small></div><div class="row-right"><b>${Math.round((x.confidence||0)*100)}%</b><small>confidence</small></div></article>`).join(""):'<div class="empty">No active recommendations.</div>'}catch{}
}
async function loadServerInfo(){try{const d=await getJson("/api/server");document.getElementById("serverInfo").innerHTML=Object.entries(d).map(([k,v])=>`<div class="info"><span>${esc(k)}</span><b>${esc(v)}</b></div>`).join("")}catch{}}
function loadUptime(){getJson("/api/uptime").then(d=>set("uptime",d.formatted||"—")).catch(()=>set("uptime","—"))}
document.querySelectorAll(".nav[data-target]").forEach(b=>b.onclick=()=>{document.querySelectorAll(".nav").forEach(x=>x.classList.remove("active"));b.classList.add("active");document.querySelectorAll(".view").forEach(x=>x.classList.remove("active"));document.getElementById(b.dataset.target).classList.add("active");set("pageTitle",b.querySelector("span").textContent)});
connectRealtime();loadHotspots();loadRedstone();loadEntities();loadTiles();loadRecommendations();loadServerInfo();loadUptime();setInterval(loadUptime,5000);


async function adminPost(endpoint, payload) {
    const response = await fetch(API + endpoint, {
        method: "POST",
        headers: {
            ...headers(),
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.error || ("HTTP " + response.status));
    return data;
}

async function destroyLagMachine() {
    const resultBox = document.getElementById("adminResult");
    const world = document.getElementById("adminWorld").value.trim();
    const x = Number(document.getElementById("adminX").value);
    const y = Number(document.getElementById("adminY").value);
    const z = Number(document.getElementById("adminZ").value);
    const player = document.getElementById("adminPlayer").value.trim();
    const autoBan = document.getElementById("adminAutoBan").checked;

    if (!world || !Number.isInteger(x) || !Number.isInteger(y) || !Number.isInteger(z)) {
        resultBox.textContent = "Enter a valid world and integer coordinates.";
        return;
    }

    if (!confirm("Destroy the configured lag-machine area at " + world + " " + x + "," + y + "," + z + "?")) return;

    resultBox.textContent = "Executing admin action...";
    try {
        const data = await adminPost("/api/admin/destroy", { world, x, y, z, player: player || null, autoBan });
        resultBox.textContent = "Destroyed " + data.blocksBroken + " block(s). Sign placed: " + data.signPlaced + ". Banned: " + data.banned + ".";
        loadHotspots();
        loadRedstone();
    } catch (error) {
        resultBox.textContent = "Admin action failed: " + error.message;
    }
}

async function tempBanPlayer() {
    const resultBox = document.getElementById("adminResult");
    const player = document.getElementById("adminPlayer").value.trim();
    if (!player) {
        resultBox.textContent = "Enter a player name first.";
        return;
    }
    if (!confirm("Tempban " + player + " using the configured duration and reason?")) return;
    resultBox.textContent = "Applying tempban...";
    try {
        const data = await adminPost("/api/admin/ban", { player });
        resultBox.textContent = data.success
            ? "Tempbanned " + data.player + " for " + data.durationDays + " day(s). Reason: " + data.reason
            : "Tempban failed.";
    } catch (error) {
        resultBox.textContent = "Tempban failed: " + error.message;
    }
}
