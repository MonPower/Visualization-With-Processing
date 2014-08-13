import g4p_controls.*;

int SQUARE_FLOW_FIELD_SZ = 193;
int WINDOWSIZE = SQUARE_FLOW_FIELD_SZ*3;
int n_xres = SQUARE_FLOW_FIELD_SZ;
int n_yres = SQUARE_FLOW_FIELD_SZ;
int MAXPARTICLE = n_xres*n_yres;
int numParticle = 0;
int generate = 0;
ArrayList particle = new ArrayList();
float[] pVectr = new float[2*n_xres*n_yres];
boolean windowNotExist = true;
GWindow newWindow;
GCustomSlider slider;
PImage map;
PImage wind;
PImage googlemap;
float opacity = 0.8;
double lat = 21.3115;
double lon = -157.7964;
int zoom_size = 7;
String map_type = "satellite";


void setup() {
  background(0);
  map = loadImage("./image/hawaii.jpg");
  wind = loadImage("./image/wind.jpg");
  size(WINDOWSIZE,WINDOWSIZE);
  createSlider();
  readVector(n_xres, n_yres, pVectr);
  NormalizVectrs(n_xres, n_yres, pVectr);
  for (int i = 1; i <= n_xres; i++)
    for (int j = 1; j <= n_yres; j++)
    {
      Particle p = new Particle();
      p.x = i*3-1.5;
      p.y = j*3-1.5;
      if (i % 2 ==0 || j % 2 == 0 || i % 3 == 0 || j % 3 == 0)
        p.life = 0;
      particle.add(p);
      numParticle++;
    }
    googlemap = loadImage("http://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon+ "&zoom=" + zoom_size + "&key=AIzaSyBq9EAjubBJETt31qL5o0uf5f4DszoVHcY&size=600x600&maptype=" + map_type + "&sensor=false","jpg");
}

void createSlider() {
  slider = new GCustomSlider(this, 0, 10, 150, 10, null);
  slider.setValue(0.8);
}

void readVector(int n_xres, int n_yres, float pVectr[]) {
  float vx, vy;
  int index;
  int line = 0;
  String data[] = loadStrings("wind.txt");
  for (int i = n_xres-1; i >= 0; i--)
    for (int j = 0; j < n_yres; j++)
    {
      String number[] = split(data[line], ' ');
      vx = float(number[0]);
      vy = float(number[1]);
      index = (i * n_yres + j) << 1;
      pVectr[index] = vx;
      pVectr[index+1] = -vy;
      line++;
    }
}

void NormalizVectrs(int n_xres, int n_yres, float pVectr[]) {
  for(int i = 0; i < n_xres; i ++)
    for(int j = 0; j < n_yres; j ++)
      {
        int index = (i * n_yres + j) << 1;
        float vcMag = sqrt(pVectr[index] * pVectr[index] + pVectr[index + 1] * pVectr[index + 1]);
        float scale = (vcMag == 0.0f) ? 0.0f : 1.0f / vcMag;
        pVectr[index] *= scale;
        pVectr[index+1] *= scale;
      }
}


void draw() {
  if (keyPressed) {
    if ((key == 'm' || key == 'M') && windowNotExist) {
      createWindow();
      windowNotExist = false;
      keyPressed = false;
    }
  }
  tint(255,20);
  image(googlemap, 0, 0, width, height);
  opacity = slider.getValueF();
  // fill(0,15);
  // rect(0,0,width,height);
  for (int i = 0; i < particle.size(); i++)
  {
    Particle p = (Particle)particle.get(i);
    int posx = (int)(p.x / 3);
    int posy = (int)(p.y / 3);
    //print(posx + " " + posy);
    if (p.x >= 193*3 || p.y >= 193*3 || p.x <= 0 || p.y <= 0)
    {
      p.life = 0;
      numParticle--;
    }
    if (p.life != 0) {
      int index = (posy * n_xres + posx) << 1;
      p.vx = pVectr[index];
      p.vy = pVectr[index+1];
      p.update();
      p.paint();
    }
    else if (numParticle < MAXPARTICLE){
      particle.remove(i);
      int m,n;
      if (generate % 2 == 0)
      {
        m = (int)random(0,5);
        n = (int)random(0,n_yres);
        generate++;
      }
      else {
        m = (int)random(0,n_xres);
        n = (int)random(0,5);
        generate++;
      }
      Particle q = new Particle();
      q.x = m*3-1.5;
      q.y = n*3-1.5;
      particle.add(q);
      numParticle++;
    }
  }
  // println(particle[0].life);
}

void createWindow() {
   newWindow = new GWindow(this, "Streamline-LIC", WINDOWSIZE+50, 0, WINDOWSIZE, WINDOWSIZE, false, JAVA2D);
   newWindow.addOnCloseHandler(this, "closeWindow");
   newWindow.addDrawHandler(this, "drawWindow");
   newWindow.setActionOnClose(GWindow.CLOSE_WINDOW);
}

void closeWindow(GWindow window) {
  windowNotExist = true;
}

void drawWindow(GWinApplet appc, GWinData data){
  appc.image(map,0,0);
  appc.tint(255,opacity*255);
  appc.image(wind,0,0);
  appc.noTint();
}

class Particle {
  float x, y;
  float vx, vy;
  int life;

  Particle() {
    life = 1;
  }

  void update() {
    x += vx;
    y += vy;
  }

  void paint() {
      noStroke();
      fill(255);
      ellipse(x,y,1.2,1.2);
  }
}