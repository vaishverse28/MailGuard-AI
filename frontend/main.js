const user=JSON.parse(localStorage.getItem("mg_user")||"null");
    if(!user)location.replace("../index.html");
document.querySelectorAll("[data-user-name]").forEach(e=>e.textContent=user?.name||"User");
document.querySelectorAll("[data-avatar]").forEach(e=>e.textContent=(user?.name||"U").charAt(0).toUpperCase());
const current=(location.pathname.split("/").pop()||"dashboard.html").replace(".html","");
document.querySelectorAll("[data-page-title]").forEach(e=>e.textContent=current.charAt(0).toUpperCase()+current.slice(1));
document.querySelectorAll("[data-nav]").forEach(a=>{if(a.dataset.nav===current)a.classList.add("active")});
document.querySelectorAll("[data-logout]").forEach(b=>b.addEventListener("click",()=>{localStorage.removeItem("mg_user");
    location.href="../index.html";}));
document.getElementById("menuBtn")?.addEventListener("click",()=>document.getElementById("sidebar")?.classList.toggle("open"));
const getHistory=()=>JSON.parse(localStorage.getItem("mg_history")||"[]");
const saveHistory=data=>localStorage.setItem("mg_history",JSON.stringify(data));
function badge(status){return <span class="status ${status}">${status}</span>}
function openResult(id){localStorage.setItem("mg_selected",id);
    location.href="result.html"}
function formatDate(d){return new Date(d).toLocaleString();}
function renderRecent(){const rows=getHistory().slice(0,5),body=document.getElementById("recentTable"),empty=document.getElementById("recentEmpty");if(!body)return;
    body.innerHTML=rows.map(r=><tr><td>${escapeHtml(r.subject)}</td><td>${escapeHtml(r.sender)}</td><td>${badge(r.status)}</td><td>${formatDate(r.date)}</td><td><button class="small-action" data-view="${r.id}">View</button></td></tr>).join("");
empty.style.display=rows.length?"none":"block";body.querySelectorAll("[data-view]").forEach(b=>b.addEventListener("click",()=>openResult(b.dataset.view)));}
function escapeHtml(s=""){return String(s).replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39"}[c]));}
function updateStats(){const h=getHistory(),safe=h.filter(x=>x.status==="safe").length,threat=h.length-safe;
const set=(id,v)=>{const el=document.getElementById(id);
        if(el)el.textContent=v};
set("statTotal",h.length);
set("statSafe",safe);
set("statSpam",h.filter(x=>x.status!=="safe").length);set("statThreat",h.length?Math.round(threat/h.length*100)+"%":"0%");set("anTotal",h.length);set("anSafe",h.length?Math.round(safe/h.length*100)+"%":"0%");
set("anSpam",h.length?Math.round(threat/h.length*100)+"%":"0%");
set("anThreat",threat);
renderRecent();}
function loadResult(){
const id=localStorage.getItem("mg_selected"),r=getHistory().find(x=>x.id===id);if(!r)return;const set=(id,v)=>{const e=document.getElementById(id);
    if(e)e.textContent=v};
    set("resultStatus",r.status.toUpperCase()+" EMAIL");
    set("resultSummary",${r.subject} . From ${r.sender});
    set("riskScore",r.risk+"%");
const ring=document.getElementById("riskRing");
    if(ring)ring.style.borderColor=r.status==="safe"?"#16a34a":r.status==="phishing"?"#ea580c":"#dc2626";
const words=document.getElementById("suspiciousWords");
    if(words)words.innerHTML=(r.words.length?r.words:["No obvious suspicious terms found"]).map(w=><span class="word-chip">${escapeHtml(w)}</span>).join("");
    set("aiExplanation",r.explanation);
const rec=document.getElementById("recommendations");
    if(rec)rec.innerHTML=r.recommendations.map(x=><li>${escapeHtml(x)}</li>).join("");}
document.addEventListener("DOMContentLoaded",()=>{updateStats();
    loadResult();});