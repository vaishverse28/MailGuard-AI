document.addEventListener("DOMContentLoaded",()=>{const h=JSON.parse(localStorage.getItem("mg_history")||"[]"),safe=h.filter(x=>x.status==="safe").length,threat=h.length-safe;
document.getElementById("threatSummary").textContent=${threat} potential threat(s) and ${safe} safe email(s) recorded from ${h.length} total scan(s);
    if(!window.Chart||!h.length)return;
document.getElementById("pieFallback").style.display="none";
document.getElementById("barFallback").style.display="none";
    new Chart(document.getElementById("analyticsPie"),{type:"pie",data:{labels:["Safe","Spam / phishing"],datasets:[{data:[safe,threat],backgroundColor:["#16a34a","#dc2626"]}]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{position:"bottom"}}}});
const days=Array.from({length:7},(_,i)=>{const d=new Date();
    d.setDate(d.getDate()-6+i);return d});
    new Chart(document.getElementById("analyticsBar"),{type:"bar",data:{labels:days.map(d=>d.toLocaleDateString("en",{weekday:"short"})),datasets:[{label:"Scans",data:days.map(d=>h.filter(x=>new Date(x.date).toDateString()===d.toDateString()).length),backgroundColor:"#2563eb",borderRadius:6}]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false}}}});});