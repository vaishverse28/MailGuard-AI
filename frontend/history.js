function renderHistory(){
    const all=JSON.parse(localStorage.getItem("mg_history")||"[]"),q=(document.getElementById("historySearch").value||"").toLowerCase(),filter=document.getElementById("historyFilter").value,sort=document.getElementById("historySort").value;
    let rows=all.filter(r=>(r.sender+" "+r.subject).toLowerCase().includes(q)&&(filter==="all"||r.status===filter));
       rows.sort((a,b)=>sort==="newest"?new Date(b.date)-new Date(a.date):new Date(a.date)-new Date(b.date));
    const body=document.getElementById("historyTable");
       body.innerHTML=rows.map((r,i)=>
       <tr>
        <td>${i+1}</td>
        <td>${escapeHtml(r.sender)}</td>
        <td>${escapeHtml(r.subject)}</td><td>${badge(r.status)}</td><td>${r.risk}%</td>
        <td>${formatDate(r.date)}</td>
        <td><button class="small-action" data-view="${r.id}">View</button>
            <button class="small-action delete" data-delete="${r.id}">Delete</button></td>
       </tr>).join("");
    document.getElementById("historyEmpty").style.display=rows.length?"none":"block";body.querySelectorAll("[data-view]").forEach(b=>b.onclick=()=>openResult(b.dataset.view));
    body.querySelectorAll("[data-delete]").forEach(b=>b.onclick=()=>{if(confirm("Delete this email record?")){localStorage.setItem("mg_history",JSON.stringify(all.filter(x=>x.id!==b.dataset.delete)));
        renderHistory();}});}["historySearch","historyFilter","historySort"].forEach(id=>document.getElementById(id)?.addEventListener("input",renderHistory));
    document.addEventListener("DOMContentLoaded",renderHistory);