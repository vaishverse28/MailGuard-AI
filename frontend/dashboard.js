document.addEventListener("DOMContentLoaded",()=>{const h=JSON.parse(localStorage.getItem("mg_history")||"[]"),safe=h.filter(x=>x.status==="safe").length,threat=h.length-safe;
    const fallback=document.getElementById("chartFallback"),wf=document.getElementById("weeklyFallback");
    if(window.Chart&&h.length){fallback.style.display="none";
        new Chart(document.getElementById("overviewChart"),{type:"doughnut",data:{labels:["Safe","Threat"],datasets:[{data:[safe,threat],backgroundColor:["#16a34a","#dc2626"],borderWidth:0}]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{position:"bottom"}}}});
    const days=Array.from({length:7},(_,i)=>{const d=new Date();d.setDate(d.getDate()-(6-i));return d});
    const counts=days.map(d=>h.filter(x=>new Date(x.date).toDateString()===d.toDateString()).length);
        wf.style.display="none";
        new Chart(document.getElementById("weeklyChart"),{type:"line",data:{labels:days.map(d=>d.toLocaleDateString("en",{weekday:"short"})),datasets:[{label:"Scans",data:counts,borderColor:"#2563eb",backgroundColor:"#2563eb22",fill:true,tension:.35}]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false}}}});}});