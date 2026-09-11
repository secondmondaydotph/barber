const {chromium}=require('C:/Users/Alvin/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const fs=require('node:fs');
(async()=>{
 const browser=await chromium.launch({channel:'msedge',headless:true});
 const root='http://localhost:8082/Barber';
 const dir='C:/ALvin/Barber/.local/screenshots';fs.mkdirSync(dir,{recursive:true});
 const page=await browser.newPage({viewport:{width:1440,height:1000}});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 for(const path of ['home','about','services','team','contact','testimonials','login','register']){
  const response=await page.goto(root+'/'+path,{waitUntil:'networkidle'});
  if(response.status()!==200)throw Error(path+' returned '+response.status());
 }
 await page.goto(root+'/home',{waitUntil:'networkidle'});await page.evaluate(async()=>{for(let y=0;y<document.body.scrollHeight;y+=600){scrollTo(0,y);await new Promise(r=>setTimeout(r,120));}scrollTo(0,0)});await page.waitForTimeout(1200);await page.screenshot({path:dir+'/home-desktop.png',fullPage:true});
 await page.setViewportSize({width:400,height:861});await page.screenshot({path:dir+'/home-phone.png',fullPage:true});
 const overflow=await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth);if(overflow)throw Error('Home page overflows at 400px');
 await page.goto(root+'/register',{waitUntil:'networkidle'});await page.evaluate(async()=>{for(let y=0;y<document.body.scrollHeight;y+=600){scrollTo(0,y);await new Promise(r=>setTimeout(r,120));}scrollTo(0,0)});await page.waitForTimeout(1000);await page.screenshot({path:dir+'/register-phone.png',fullPage:true});
 await page.goto(root+'/admin/barbers');if(!page.url().endsWith('/login'))throw Error('Anonymous admin request was not redirected');
 const response=await page.request.post(root+'/register',{form:{email:'qa@example.test'}});if(response.status()!==403)throw Error('CSRF request was not rejected');
 console.log(JSON.stringify({publicPages:'8 passed',anonymousAdmin:'blocked',csrf:'blocked',javascriptErrors:errors},null,2));
 await browser.close();if(errors.length)process.exitCode=1;
})().catch(e=>{console.error(e);process.exitCode=1});
