const {chromium}=require('C:/Users/Alvin/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const fs=require('node:fs'),crypto=require('node:crypto'),{spawnSync}=require('node:child_process');
const root='http://localhost:8082/Barber',dir='C:/ALvin/Barber',token=crypto.randomBytes(6).toString('hex'),name='QA '+token+' ',password=crypto.randomBytes(24).toString('base64url');
const loginFile=fs.readFileSync(dir+'/.local/admin-login.txt','utf8');const adminEmail=/Email: (.+)/.exec(loginFile)[1].trim(),adminPassword=/Password: (.+)/.exec(loginFile)[1].trim();
let browser;
function check(v,message){if(!v)throw Error(message)}
async function csrf(page){return await page.locator('input[name=csrf]').first().inputValue()}
async function post(page,path,fields){return await page.request.post(root+path,{form:{csrf:await csrf(page),...fields}})}
async function login(page,email,secret){await page.goto(root+'/login');await page.locator('input[name=email]').fill(email);await page.locator('input[name=password]').fill(secret);await Promise.all([page.waitForURL(u=>!u.pathname.endsWith('/login')),page.getByRole('button',{name:'Sign in',exact:true}).click()])}
async function itemId(page,path,item){await page.goto(root+path);return await page.locator('form').filter({has:page.locator('input[name=name][value="'+item+'"]')}).locator('input[name=id]').inputValue()}
async function register(page,suffix){const email='qa-'+token+'-'+suffix+'@example.test';await page.goto(root+'/register');await post(page,'/register',{name:name+suffix,email,phone:'0000000000',password});await login(page,email,password);return email}
async function mine(page){await page.goto(root+'/my-bookings');return await page.locator('tbody tr').filter({hasText:name+'service'}).count()}
(async()=>{try{
 browser=await chromium.launch({channel:'msedge',headless:true});
 const adminContext=await browser.newContext();const admin=await adminContext.newPage();const errors=[];admin.on('pageerror',e=>errors.push(e.message));admin.on('console',m=>{if(m.type()==='error'&&/Invalid scale|not a registered/.test(m.text()))errors.push(m.text())});
 await login(admin,adminEmail,adminPassword);check(admin.url().endsWith('/dashboard'),'Admin dashboard missing');
 await post(admin,'/admin/barber',{name:name+'barber',email:'qa-'+token+'-barber@example.test',password,active:'on'});const barber=await itemId(admin,'/admin/barbers',name+'barber');
 await post(admin,'/admin/service',{name:name+'service',minutes:'30',price:'123.45',active:'on'});const service=await itemId(admin,'/admin/services',name+'service');
 await post(admin,'/admin/addon',{name:name+'addon',minutes:'15',price:'10.00',active:'on'});const addon=await itemId(admin,'/admin/addons',name+'addon');
 const date=new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Manila',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date(Date.now()+86400000));const weekday=new Date(date+'T12:00:00Z').getUTCDay()||7;
 await post(admin,'/admin/schedule',{barber,weekday:String(weekday),start:'09:00',end:'18:00',active:'on'});
 const customer1=await (await browser.newContext()).newPage(),customer2=await (await browser.newContext()).newPage();await register(customer1,'customer1');await register(customer2,'customer2');
 check((await customer1.request.get(root+'/admin/barbers')).status()===403,'Customer accessed admin page');
 await customer1.goto(root+'/book');check((await customer1.locator('#service').innerText()).includes('—'),'Booking label UTF-8 encoding failed');const query=new URLSearchParams({barber,service,date,addon});const available=await (await customer1.request.get(root+'/availability?'+query)).json();check(available.slots.includes('09:00'),'Configured availability missing');
 const fields={barber,service,date,addon,time:'09:00',total:'0.01',price:'0.01'};
 await Promise.all([post(customer1,'/book',fields),post(customer2,'/book',fields)]);
 const n1=await mine(customer1),n2=await mine(customer2);check(n1+n2===1,'Concurrent booking did not enforce a single winner');const winner=n1?customer1:customer2,loser=n1?customer2:customer1;
 const row=winner.locator('tbody tr').filter({hasText:name+'service'});check((await row.innerText()).includes('133.45'),'Server price calculation accepted tampered total');const booking=await row.locator('input[name=id]').inputValue();
 check((await post(loser,'/cancel-booking',{id:booking})).status()===403,'Customer could cancel another account booking');
 await admin.goto(root+'/dashboard');await post(admin,'/admin/barber',{id:barber,name:name+'barber',email:'qa-'+token+'-barber@example.test',password:''});await admin.goto(root+'/admin/barbers');check(await admin.locator('form').filter({has:admin.locator('input[name=id][value="'+barber+'"]')}).locator('input[name=active]').isChecked(),'Barber deactivated with future appointment');
 const staff=await (await browser.newContext()).newPage();await login(staff,'qa-'+token+'-barber@example.test',password);check((await staff.request.get(root+'/admin/services')).status()===403,'Staff accessed admin catalog');
 await post(staff,'/staff/status',{id:booking,status:'IN_PROGRESS'});await post(staff,'/staff/status',{id:booking,status:'COMPLETED'});
 await winner.goto(root+'/my-bookings');check((await winner.locator('tbody').innerText()).includes('COMPLETED'),'Status workflow failed');
 await post(winner,'/book',{...fields,time:'10:00'});await winner.goto(root+'/my-bookings');const nextId=await winner.locator('tbody form input[name=id]').first().inputValue();await post(winner,'/cancel-booking',{id:nextId});await winner.goto(root+'/my-bookings');check((await winner.locator('tbody').innerText()).includes('CANCELLED'),'Customer cancellation failed');
 await post(admin,'/admin/exception',{barber,date,closed:'on'});const closed=await (await winner.request.get(root+'/availability?'+query)).json();check(closed.slots.length===0,'Closed-day exception ignored');
 await admin.setViewportSize({width:1440,height:1000});await admin.goto(root+'/dashboard',{waitUntil:'networkidle'});await admin.screenshot({path:dir+'/.local/screenshots/dashboard-desktop.png',fullPage:true});
 await admin.setViewportSize({width:820,height:1180});await admin.screenshot({path:dir+'/.local/screenshots/dashboard-tablet.png',fullPage:true});check(await admin.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'Dashboard overflows tablet width');
 await winner.goto(root+'/book',{waitUntil:'networkidle'});await winner.setViewportSize({width:400,height:861});await winner.screenshot({path:dir+'/.local/screenshots/booking-phone.png',fullPage:true});
 check(errors.length===0,'Dashboard JavaScript errors: '+errors.join('; '));
 console.log('PASS: registration, login, role boundaries, admin catalog, schedules, price integrity, concurrent booking, ownership, staff workflow, cancellation, closed dates and dashboard rendering.');
 }finally{
  if(browser)await browser.close();const result=spawnSync('C:/Program Files/Eclipse Adoptium/jdk-21.0.12.8-hotspot/bin/java.exe',['-Dbarber.config='+dir+'/.local/database.properties','-cp',dir+'/build/classes;'+dir+'/build/tests;'+dir+'/WebContent/WEB-INF/lib/postgresql.jar','com.barber.FixtureCleanup',token],{encoding:'utf8'});console.log(result.stdout);if(result.status!==0)throw Error('QA fixture cleanup failed; inspect test run '+token);
 }
})().catch(e=>{console.error(e.message);process.exitCode=1});
