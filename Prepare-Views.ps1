$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$views=Join-Path $root 'WebContent\WEB-INF\views'
$public=Join-Path $root 'sourceTemplate\Salone'
$dashboard=Join-Path $root 'sourceTemplate\bootstrap-admin-template-free\bootstrap-admin-template-free'
$utf8=[Text.UTF8Encoding]::new($false)
$directive='<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>'
function Public-Template([string]$html) {
 $html=$html.Replace('href="css/','href="${pageContext.request.contextPath}/assets/public/css/').Replace('href="lib/','href="${pageContext.request.contextPath}/assets/public/lib/').Replace('src="lib/','src="${pageContext.request.contextPath}/assets/public/lib/').Replace('src="js/','src="${pageContext.request.contextPath}/assets/public/js/').Replace('src="img/','src="${pageContext.request.contextPath}/assets/public/img/').Replace('href="img/favicon.ico"','href="${pageContext.request.contextPath}/assets/public/img/hero-slider-1.jpg"')
 foreach($mapping in @(@('index.html','home'),@('about.html','about'),@('service.html','services'),@('team.html','team'),@('testimonial.html','testimonials'),@('contact.html','contact'),@('404.html','my-bookings'))) {$html=$html.Replace('href="'+$mapping[0]+'"','href="${pageContext.request.contextPath}/'+$mapping[1]+'"')}
 $html=$html.Replace('<a href="${pageContext.request.contextPath}/my-bookings" class="dropdown-item">My appointments</a>','<a href="${pageContext.request.contextPath}/my-bookings" class="dropdown-item">My appointments</a><a href="${pageContext.request.contextPath}/profile" class="dropdown-item">My profile</a>')
 $html=$html.Replace('Salone','Barber').Replace('Your Site Name','Barber').Replace('404 Page','My appointments')
 $html=$html.Replace('<title>Barber - Beauty Salon Website Template</title>','<title>Barber | Appointments and grooming</title>')
 $html=$html.Replace('Beauty Salon Fashion for Women','Modern Grooming for Everyone')
 $html=$html.Replace('123 Street, New York, USA','245 Esmeralda St, San Andres Bukid, Metro Manila, Philippines')
 $html=$html.Replace('<title>Barber | Appointments and grooming</title>','<title>Limpio Holič | Appointments and grooming</title>')
 $html=$html.Replace('<h1 class="mb-0"><i class="bi bi-scissors"></i>Barber</h1>','<span class="brand-lockup"><img src="${pageContext.request.contextPath}/assets/public/img/brand-logo.png" alt="Limpio Holič logo" width="56" height="56" style="width:56px;height:56px;object-fit:contain"><span>Limpio Holič</span></span>')
 $html=$html.Replace('<h1 class="display-5 text-primary mb-0"><i class="bi bi-scissors"></i>Barber</h1>','<h1 class="display-5 text-primary mb-0">Limpio Holič</h1>')
 $html=$html.Replace('Hair Specialist','Master Barber').Replace('Nail Designer','Barber &amp; Stylist').Replace('Beauty Specialist','Grooming Specialist').Replace('Spa Specialist','Senior Barber')
 $html=$html.Replace('Lily Taylor','Marco Santos').Replace('Olivia Smith','Mia Reyes').Replace('Ava Brown','Paolo Cruz').Replace('Amelia Jones','Nico Garcia')
 # Keep the original service-card layout, but use neutral barber pictograms and barber services.
 $html=$html.Replace('<img class="img-fluid" src="${pageContext.request.contextPath}/assets/public/img/haircut.png" alt="">','<div class="barber-service-icon" aria-hidden="true">CUT</div>')
 $html=$html.Replace('<img class="img-fluid" src="${pageContext.request.contextPath}/assets/public/img/makeup.png" alt="">','<div class="barber-service-icon" aria-hidden="true">STYLE</div>').Replace('<h3 class="mb-3">Makeup</h3>','<h3 class="mb-3">Hair Styling</h3>')
 $html=$html.Replace('<img class="img-fluid" src="${pageContext.request.contextPath}/assets/public/img/manicure.png" alt="">','<div class="barber-service-icon" aria-hidden="true">BEARD</div>').Replace('<h3 class="mb-3">Manicure</h3>','<h3 class="mb-3">Beard Trim</h3>')
 $html=$html.Replace('<img class="img-fluid" src="${pageContext.request.contextPath}/assets/public/img/pedicure.png" alt="">','<div class="barber-service-icon" aria-hidden="true">KIDS</div>').Replace('<h3 class="mb-3">Pedicure</h3>',"<h3 class=`"mb-3`">Kids' Haircut</h3>")
 $html=$html.Replace('<img class="img-fluid" src="${pageContext.request.contextPath}/assets/public/img/massage.png" alt="">','<div class="barber-service-icon" aria-hidden="true">SHAVE</div>').Replace('<h3 class="mb-3">Massage</h3>','<h3 class="mb-3">Hot Towel Shave</h3>')
 $html=$html.Replace('<img class="img-fluid" src="${pageContext.request.contextPath}/assets/public/img/skin-care.png" alt="">','<div class="barber-service-icon" aria-hidden="true">FULL</div>').Replace('<h3 class="mb-3">Skin Care</h3>','<h3 class="mb-3">Grooming Package</h3>')
 $html=$html.Replace('<a class="btn btn-sm btn-primary" href="https://htmlcodex.com/downloading/?item=3597">Buy Pro Version</a>',@'
<div class="d-flex flex-wrap gap-2"><a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/book">Book now</a>
<% if(session.getAttribute("user")==null){ %><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/login">Sign in</a><% }else{ %><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/profile">My account</a><form method="post" action="${pageContext.request.contextPath}/logout"><input type="hidden" name="csrf" value="<%=com.barber.util.Html.e(session.getAttribute("csrf"))%>"><button class="btn btn-sm btn-outline-primary">Sign out</button></form><% } %></div>
'@)
 $html=$html.Replace('</head>','<link href="${pageContext.request.contextPath}/assets/public/css/barber-theme.css?v=20260905b" rel="stylesheet"></head>')
 $html=$html.Replace('</body>','<script src="${pageContext.request.contextPath}/assets/barber.js"></script></body>')
 $html=$html.Replace('https://ajax.googleapis.com/ajax/libs/jquery/3.6.1/jquery.min.js','${pageContext.request.contextPath}/assets/vendor/jquery.min.js').Replace('https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js','${pageContext.request.contextPath}/assets/vendor/bootstrap.bundle.min.js')
 # Preserve template layout while marking the unconnected newsletter honestly.
 $html=$html.Replace('placeholder="Enter Your Email"','placeholder="Newsletter not connected" disabled')
 return $directive+"`n"+$html
}
$map=@{home='index';about='about';services='service';team='team';contact='contact';testimonials='testimonial'}
foreach($name in $map.Keys){$html=Public-Template ([IO.File]::ReadAllText((Join-Path $public ($map[$name]+'.html'))));[IO.File]::WriteAllText((Join-Path $views ($name+'.jsp')),$html,$utf8)}
$teamPath=Join-Path $views 'team.jsp'
$teamHtml=[IO.File]::ReadAllText($teamPath)
$teamHtml=[regex]::Replace($teamHtml,'(?s)<!-- Team Start -->.*?<!-- Team End -->','<%@ include file="team-content.jspf" %>')
[IO.File]::WriteAllText($teamPath,$teamHtml,$utf8)
$form=Public-Template ([IO.File]::ReadAllText((Join-Path $public 'contact.html')))
$form=$form.Replace('<body>','<body class="barber-form-page">')
$form=[regex]::Replace($form,'(?s)<!-- Hero Start -->.*?<!-- Hero End -->','')
$form=[regex]::Replace($form,'(?s)<!-- Contact Start -->.*?<!-- Contact End -->','<%@ include file="form-content.jspf" %>')
[IO.File]::WriteAllText((Join-Path $views 'public-form.jsp'),$form,$utf8)
$admin=[IO.File]::ReadAllText((Join-Path $dashboard 'index.html'))
$admin=$admin.Replace('href="css/','href="${pageContext.request.contextPath}/assets/dashboard/css/').Replace('href="lib/','href="${pageContext.request.contextPath}/assets/dashboard/lib/').Replace('src="lib/','src="${pageContext.request.contextPath}/assets/dashboard/lib/').Replace('src="js/','src="${pageContext.request.contextPath}/assets/dashboard/js/').Replace('src="img/','src="${pageContext.request.contextPath}/assets/dashboard/img/').Replace('href="img/favicon.ico"','href="${pageContext.request.contextPath}/assets/dashboard/img/user.jpg"')
$admin=$admin.Replace('DASHMIN - Bootstrap Admin Template','Barber | Dashboard').Replace('DASHMIN','Barber').Replace('Your Site Name','Barber').Replace('href="index.html"','href="${pageContext.request.contextPath}/dashboard"').Replace('Jhon Doe','<%=com.barber.util.Html.e(((com.barber.model.User)session.getAttribute("user")).name())%>').Replace('<span>Admin</span>','<span><%=com.barber.util.Html.e(((com.barber.model.User)session.getAttribute("user")).role())%></span>')
$menu=@'
<div class="navbar-nav w-100">
<a href="${pageContext.request.contextPath}/dashboard" class="nav-item nav-link"><i class="fa fa-tachometer-alt me-2"></i>Dashboard</a>
<a href="${pageContext.request.contextPath}/staff/bookings" class="nav-item nav-link"><i class="fa fa-calendar me-2"></i>Appointments</a>
<% if(((com.barber.model.User)session.getAttribute("user")).role().equals("ADMIN")){ %>
<a href="${pageContext.request.contextPath}/admin/customers" class="nav-item nav-link"><i class="fa fa-users me-2"></i>Customers</a>
<a href="${pageContext.request.contextPath}/admin/barbers" class="nav-item nav-link"><i class="fa fa-user me-2"></i>Barbers</a>
<a href="${pageContext.request.contextPath}/admin/services" class="nav-item nav-link"><i class="fa fa-cut me-2"></i>Services</a>
<a href="${pageContext.request.contextPath}/admin/addons" class="nav-item nav-link"><i class="fa fa-plus me-2"></i>Add-ons</a>
<a href="${pageContext.request.contextPath}/admin/schedules" class="nav-item nav-link"><i class="fa fa-clock me-2"></i>Schedules</a>
<% } %><a href="${pageContext.request.contextPath}/home" class="nav-item nav-link"><i class="fa fa-home me-2"></i>Public site</a>
</div></nav>
'@
$admin=[regex]::Replace($admin,'(?s)<div class="navbar-nav w-100">.*?</nav>',$menu)
$account=@'
<div class="navbar-nav align-items-center ms-auto"><div class="nav-item dropdown"><a href="#" class="nav-link dropdown-toggle" data-bs-toggle="dropdown"><img class="rounded-circle me-lg-2" src="${pageContext.request.contextPath}/assets/dashboard/img/user.jpg" alt="Profile" style="width:40px;height:40px"><span class="d-none d-lg-inline-flex"><%=com.barber.util.Html.e(((com.barber.model.User)session.getAttribute("user")).name())%></span></a><div class="dropdown-menu dropdown-menu-end bg-light border-0 rounded-0 rounded-bottom m-0"><span class="dropdown-item-text"><%=com.barber.util.Html.e(((com.barber.model.User)session.getAttribute("user")).email())%></span><form method="post" action="${pageContext.request.contextPath}/logout"><input type="hidden" name="csrf" value="<%=com.barber.util.Html.e(session.getAttribute("csrf"))%>"><button class="dropdown-item">Sign out</button></form></div></div></div></nav>
'@
$admin=[regex]::Replace($admin,'(?s)<div class="navbar-nav align-items-center ms-auto">.*?</nav>',$account)
$admin=[regex]::Replace($admin,'(?s)<form class="d-none d-md-flex ms-4">.*?</form>','<span class="d-none d-md-flex ms-4">Barber management</span>')
$admin=[regex]::Replace($admin,'(?s)<!-- Sale & Revenue Start -->.*?<!-- Widgets End -->','<%@ include file="dashboard-content.jspf" %>')
$admin=$admin.Replace('https://code.jquery.com/jquery-3.4.1.min.js','${pageContext.request.contextPath}/assets/vendor/jquery.min.js').Replace('https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js','${pageContext.request.contextPath}/assets/vendor/bootstrap.bundle.min.js').Replace('</body>','<script src="${pageContext.request.contextPath}/assets/barber.js"></script></body>')
[IO.File]::WriteAllText((Join-Path $views 'dashboard.jsp'),$directive+"`n"+$admin,$utf8)
# Keep original dashboard behaviors; its demo charts need guards on pages without chart canvases.
$main=[IO.File]::ReadAllText((Join-Path $dashboard 'js\main.js'))
$main=$main.Replace('    // Worldwide Sales Chart',"    if (!document.getElementById('worldwide-sales')) return;`n    // Worldwide Sales Chart")
[IO.File]::WriteAllText((Join-Path $root 'WebContent\assets\dashboard\js\main.js'),$main,$utf8)
Write-Output 'Prepared JSP pages from the original templates. Original sourceTemplate files are unchanged.'
