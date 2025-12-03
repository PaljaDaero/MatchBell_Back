import os
import json
import numpy as np
import requests
import xml.etree.ElementTree as ET

from tensorflow.keras.models import load_model
from tensorflow.keras.metrics import MeanSquaredError
# ==================== 원본 알고리즘 영역 (hd2.ipynb 에서 복사해옴) ====================
import socket
from _thread import *

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

calendarFile = os.path.join(BASE_DIR, 'cal.csv')
sky = {'갑':1,'을':2,'병':3,'정':4,'무':5,'기':6,'경':7,'신':8,'임':9,'계':10}
earth = {'자':1,'축':2,'인':3,'묘':4,'진':5,'사':6,'오':7,'미':8,'신':9,'유':10,'술':11,'해':12}
model0 = load_model(os.path.join(BASE_DIR, 'sky3000.h5'),
                    custom_objects={'mse': MeanSquaredError})
model1 = load_model(os.path.join(BASE_DIR, 'earth3000.h5'),
                    custom_objects={'mse': MeanSquaredError})

p1=8
p11=9.5
p2=7
p21=8.2
p3=6
p31=7.2
p41 = 10
p42 = 8
p43 = 6
p5=8
p6=8
p7=0
p71=10
p8=0
p81=10
p82=6
p83=4

def getKeybyvalue(list, val):
  for key, value in list.items():
    if value == val:
        return key
def get_one_hot(target, nb_classes):
  t =np.array(target).reshape(-1)
  res = np.eye(nb_classes)[np.array(t).reshape(-1)]
  return res.reshape(list(t.shape)+[nb_classes])

def calculate(token0, token1, gender0, gender1, s):
  score = s.item()
  a1 = token0[1]
  a2 = token0[3]
  a3 = token0[5]
  b1 = token1[1]
  b2 = token1[3]
  b3 = token1[5]
  sal0=[0,0,0,0,0,0,0,0]
  sal1=[0,0,0,0,0,0,0,0]

  if a3==3:
    if a1==6 or a1==9:
      if gender0==1:
        score -= p1
        sal0[0] += p1
      else:
        score -= p11
        sal0[0] += p11
    if a2==6 or a2==9:
      if gender0==1:
        score -= p1
        sal0[0] += p1
      else:
        score -= p11
        sal0[0] += p11
  if a3==7:
    if a1==2 or a1==5 or a1==7:
      if gender0==1:
        score -= p1
        sal0[0] += p1
      else:
        score -= p11
        sal0[0] += p11
      if a2==2 or a2==5 or a2==7:
          if gender0==1:
            score -= p1
            sal0[0] += p1
          else:
            score -= p11
            sal0[0] += p11
  if a3==2:
      if a1==7 or a1==8 or a1==11:
          if gender0==1:
            score -= p1
            sal0[0] += p1
          else:
            score -= p11
            sal0[0] += p11
      if a2==7 or a2==8 or a2==11:
          if gender0==1:
            score -= p1
            sal0[0] += p1
          else:
            score -= p11
            sal0[0] += p11
  if b3==3:
      if b1==6 or b1==9:
          if gender1==1:
            score -= p1
            sal1[0] += p1
          else:
            score -= p11
            sal1[0] += p11
      if b2==6 or b2==9:
          if gender1==1:
            score -= p1
            sal1[0] += p1
          else:
            score -= p11
            sal1[0] += p11
  if b3==7:
      if b1==2 or b1==5 or b1==7:
          if gender1==1:
            score -= p1
            sal1[0] += p1
          else:
            score -= p11
            sal1[0] += p11
      if b2==2 or b2==5 or b2==7:
          if gender1==1:
            score -= p1
            sal1[0] += p1
          else:
            score -= p11
            sal1[0] += p11
  if b3==2:
      if b1==7 or b1==8 or b1==11:
          if gender1==1:
            score -= p1
            sal1[0] += p1
          else:
            score -= p11
            sal1[0] += p11
      if b2==7 or b2==8 or b2==11:
          if gender1==1:
            score -= p1
            sal1[0] += p1
          else:
            score -= p11
            sal1[0] += p11

  if a3==1:
      if a1==10 or a2==10:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==2:
      if a1==7 or a2==7:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==3:
      if a1==8 or a2==8:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==4:
      if a1==9 or a2==9:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==5:
      if a1==12 or a2==12:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==6:
      if a1==11 or a2==11:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==7:
      if a1==2 or a2==2:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==8:
      if a1==3 or a2==3:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==9:
      if a1==4 or a2==4:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==10:
      if a1==1 or a2==1:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==11:
      if a1==6 or a2==6:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if a3==12:
      if a1==5 or a2==5:
        if gender0==1:
            score -= p2
            sal0[1] += p2
        else:
            score -= p21
            sal0[1] += p21
  if b3==1:
      if b1==10 or b2==10:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==2:
      if b1==7 or b2==7:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==3:
      if b1==8 or b2==8:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==4:
      if b1==9 or b2==9:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==5:
      if b1==12 or b2==12:
          score -= p2
          sal1[1] += p2
  if b3==6:
      if b1==11 or b2==11:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==7:
      if b1==2 or b2==2:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==8:
      if b1==3 or b2==3:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==9:
      if b1==4 or b2==4:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==10:
      if b1==1 or b2==1:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==11:
      if b1==6 or b2==6:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21
  if b3==12:
      if b1==5 or b2==5:
        if gender1==1:
            score -= p2
            sal1[1] += p2
        else:
            score -= p21
            sal1[1] += p21

  if (a1==1 and a2==10) or (a1==2 and a2==7) or (a1==3 and a2==8) or (a1==4 and a2==9) or (a1==5 and a2==12) or (a1==6 and a2==11) or (a1==10 and a2==1) or (a1==7 and a2==2) or (a1==8 and a2==3) or (a1==9 and a2==4) or (a1==12 and a2==5) or (a1==11 and a2==6):
    if gender0==1:
      score -= p2
      sal0[1] += p2
    else:
      score -= p21
      sal0[1] += p21
  if (b1==1 and b2==10) or (b1==2 and b2==7) or (b1==3 and b2==8) or (b1==4 and b2==9) or (b1==5 and b2==12) or (b1==6 and b2==11) or (b1==10 and b2==1) or (b1==7 and b2==2) or (b1==8 and b2==3) or (b1==9 and b2==4) or (b1==12 and b2==5) or (b1==11 and b2==6):
    if gender1==1:
      score -= p2
      sal1[1] += p2
    else:
      score -= p21
      sal1[1] += p21

  if a1==1:
      if a2==8:
          score -= p3
          sal0[2] += p3
      if a3==8:
          score -= p3
          sal0[2] += p3
  if a1==2:
      if a2==7:
          score -= p3
          sal0[2] += p3
      if a3==7:
          score -= p3
          sal0[2] += p3
  if a1==3:
      if a2==10:
          score -= p3
          sal0[2] += p3
      if a3==10:
          score -= p3
          sal0[2] += p3
  if a1==4:
      if a2==9:
          score -= p3
          sal0[2] += p3
      if a3==9:
          score -= p3
          sal0[2] += p3
  if a1==5:
      if a2==12:
          score -= p3
          sal0[2] += p3
      if a3==12:
          score -= p3
          sal0[2] += p3
  if a1==6:
      if a2==11:
          score -= p3
          sal0[2] += p3
      if a3==11:
          score -= p3
          sal0[2] += p3
  if a1==7:
      if a2==2:
          score -= p3
          sal0[2] += p3
      if a3==2:
          score -= p3
          sal0[2] += p3
  if a1==8:
      if a2==1:
          score -= p3
          sal0[2] += p3
      if a3==1:
          score -= p3
          sal0[2] += p3
  if a1==9:
      if a2==4:
          score -= p3
          sal0[2] += p3
      if a3==4:
          score -= p3
          sal0[2] += p3
  if a1==10:
      if a2==3:
          score -= p3
          sal0[2] += p3
      if a3==3:
          score -= p3
          sal0[2] += p3
  if a1==11:
      if a2==6:
          score -= p3
          sal0[2] += p3
      if a3==6:
          score -= p3
          sal0[2] += p3
  if a1==12:
      if a2==5:
          score -= p3
          sal0[2] += p3
      if a3==5:
          score -= p3
          sal0[2] += p3
  if a2==1:
      if a1==8:
          score -= p3
          sal0[2] += p3
      if a3==8:
          score -= p3
          sal0[2] += p3
  if a2==2:
      if a1==7:
          score -= p3
          sal0[2] += p3
      if a3==7:
          score -= p3
          sal0[2] += p3
  if a2==3:
      if a1==10:
          score -= p3
          sal0[2] += p3
      if a3==10:
          score -= p3
          sal0[2] += p3
  if a2==4:
      if a1==9:
          score -= p3
          sal0[2] += p3
      if a3==9:
          score -= p3
          sal0[2] += p3
  if a2==5:
      if a1==12:
          score -= p3
          sal0[2] += p3
      if a3==12:
          score -= p3
          sal0[2] += p3
  if a2==6:
      if a1==11:
          score -= p3
          sal0[2] += p3
      if a3==11:
          score -= p3
          sal0[2] += p3
  if a2==7:
      if a1==2:
          score -= p3
          sal0[2] += p3
      if a3==2:
          score -= p3
          sal0[2] += p3
  if a2==8:
      if a1==1:
          score -= p3
          sal0[2] += p3
      if a3==1:
          score -= p3
          sal0[2] += p3
  if a2==9:
      if a1==4:
          score -= p3
          sal0[2] += p3
      if a3==4:
          score -= p3
          sal0[2] += p3
  if a2==10:
      if a1==3:
          score -= p3
          sal0[2] += p3
      if a3==3:
          score -= p3
          sal0[2] += p3
  if a2==11:
      if a1==6:
          score -= p3
          sal0[2] += p3
      if a3==6:
          score -= p3
          sal0[2] += p3
  if a2==12:
      if a1==5:
          score -= p3
          sal0[2] += p3
      if a3==5:
          score -= p3
          sal0[2] += p3
  if a3==1:
      if a1==8:
          score -= p3
          sal0[2] += p3
      if a2==8:
          score -= p3
          sal0[2] += p3
  if a3==2:
      if a1==7:
          score -= p3
          sal0[2] += p3
      if a2==7:
          score -= p3
          sal0[2] += p3
  if a3==3:
      if a1==10:
          score -= p3
          sal0[2] += p3
      if a2==10:
          score -= p3
          sal0[2] += p3
  if a3==4:
      if a1==9:
          score -= p3
          sal0[2] += p3
      if a2==9:
          score -= p3
          sal0[2] += p3
  if a3==5:
      if a1==12:
          score -= p3
          sal0[2] += p3
      if a2==12:
          score -= p3
          sal0[2] += p3
  if a3==6:
      if a1==11:
          score -= p3
          sal0[2] += p3
      if  a2==11:
          score -=p3
          sal0[2] += p3
  if a3==7:
      if a1==2:
          score -= p3
          sal0[2] += p3
      if a2==2:
          score -= p3
          sal0[2] += p3
  if a3==8:
      if a1==1:
          score -= p3
          sal0[2] += p3
      if a2==1:
          score -= p3
          sal0[2] += p3
  if a3==9:
      if a1==4:
          score -= p3
          sal0[2] += p3
      if a2==4:
          score -= p3
          sal0[2] += p3
  if a3==10:
      if a1==3:
          score -= p3
          sal0[2] += p3
      if a2==3:
          score -= p3
          sal0[2] += p3
  if a3==11:
      if a1==6:
          score -= p3
          sal0[2] += p3
      if a2==6:
          score -= p3
          sal0[2] += p3
  if a3==12:
      if a1==5:
          score -= p3
          sal0[2] += p3
      if a2==5:
          score -= p3
          sal0[2] += p3
  if b1==1:
      if b2==8:
          score -= p3
          sal1[2] += p3
      if b3==8:
          score -= p3
          sal1[2] += p3
  if b1==2:
      if b2==7:
          score -= p3
          sal1[2] += p3
      if b3==7:
          score -= p3
          sal1[2] += p3
  if b1==3:
      if b2==10:
          score -= p3
          sal1[2] += p3
      if b3==10:
          score -= p3
          sal1[2] += p3
  if b1==4:
      if b2==9:
          score -= p3
          sal1[2] += p3
      if b3==9:
          score -= p3
          sal1[2] += p3
  if b1==5:
      if b2==12:
          score -= p3
          sal1[2] += p3
      if b3==12:
          score -= p3
          sal1[2] += p3
  if b1==6:
      if b2==11:
          score -= p3
          sal1[2] += p3
      if b3==11:
          score -= p3
          sal1[2] += p3
  if b1==7:
      if b2==2:
          score -= p3
          sal1[2] += p3
      if b3==2:
          score -= p3
          sal1[2] += p3
  if b1==8:
      if b2==1:
          score -= p3
          sal1[2] += p3
      if b3==1:
          score -= p3
          sal1[2] += p3
  if b1==9:
      if b2==4:
          score -= p3
          sal1[2] += p3
      if b3==4:
          score -= p3
          sal1[2] += p3
  if b1==10:
      if b2==3:
          score -= p3
          sal1[2] += p3
      if b3==3:
          score -= p3
          sal1[2] += p3
  if b1==11:
      if b2==6:
          score -= p3
          sal1[2] += p3
      if b3==6:
          score -= p3
          sal1[2] += p3
  if b1==12:
      if b2==5:
          score -= p3
          sal1[2] += p3
      if b3==5:
          score -= p3
          sal1[2] += p3
  if b2==1:
      if b1==8:
          score -= p3
          sal1[2] += p3
      if b3==8:
          score -= p3
          sal1[2] += p3
  if b2==2:
      if b1==7:
          score -= p3
          sal1[2] += p3
      if b3==7:
          score -= p3
          sal1[2] += p3
  if b2==3:
      if b1==10:
          score -= p3
          sal1[2] += p3
      if b3==10:
          score -= p3
          sal1[2] += p3
  if b2==4:
      if b1==9:
          score -= p3
          sal1[2] += p3
      if b3==9:
          score -= p3
          sal1[2] += p3
  if b2==5:
      if b1==12:
          score -= p3
          sal1[2] += p3
      if b3==12:
          score -= p3
          sal1[2] += p3
  if b2==6:
      if b1==11:
          score -= p3
          sal1[2] += p3
      if b3==11:
          score -= p3
          sal1[2] += p3
  if b2==7:
      if b1==2:
          score -= p3
          sal1[2] += p3
      if b3==2:
          score -= p3
          sal1[2] += p3
  if b2==8:
      if b1==1:
          score -= p3
          sal1[2] += p3
      if b3==1:
          score -= p3
          sal1[2] += p3
  if b2==9:
      if b1==4:
          score -= p3
          sal1[2] += p3
      if b3==4:
          score -= p3
          sal1[2] += p3
  if b2==10:
      if b1==3:
          score -= p3
          sal1[2] += p3
      if b3==3:
          score -= p3
          sal1[2] += p3
  if b2==11:
      if b1==6:
          score -= p3
          sal1[2] += p3
      if b3==6:
          score -= p3
          sal1[2] += p3
  if b2==12:
      if b1==5:
          score -= p3
          sal1[2] += p3
      if b3==5:
          score -= p3
          sal1[2] += p3
  if b3==1:
      if b1==8:
          score -= p3
          sal1[2] += p3
      if b2==8:
          score -= p3
          sal1[2] += p3
  if b3==2:
      if b1==7:
          score -= p3
          sal1[2] += p3
      if b2==7:
          score -= p3
          sal1[2] += p3
  if b3==3:
      if b1==10:
          score -= p3
          sal1[2] += p3
      if b2==10:
          score -= p3
          sal1[2] += p3
  if b3==4:
      if b1==9:
          score -= p3
          sal1[2] += p3
      if b2==9:
          score -= p3
          sal1[2] += p3
  if b3==5:
      if b1==12:
          score -= p3
          sal1[2] += p3
      if b2==12:
          score -= p3
          sal1[2] += p3
  if b3==6:
      if b1==11:
          score -= p3
          sal1[2] += p3
      if b2==11:
          score -=p3
          sal1[2] += p3
  if b3==7:
      if b1==2:
          score -= p3
          sal1[2] += p3
      if b2==2:
          score -= p3
          sal1[2] += p3
  if b3==8:
      if b1==1:
          score -= p3
          sal1[2] += p3
      if b2==1:
          score -= p3
          sal1[2] += p3
  if b3==9:
      if b1==4:
          score -= p3
          sal1[2] += p3
      if b2==4:
          score -= p3
          sal1[2] += p3
  if b3==10:
      if b1==3:
          score -= p3
          sal1[2] += p3
      if b2==3:
          score -= p3
          sal1[2] += p3
  if b3==11:
      if b1==6:
          score -= p3
          sal1[2] += p3
      if b2==6:
          score -= p3
          sal1[2] += p3
  if b3==12:
      if b1==5:
          score -= p3
          sal1[2] += p3
      if b2==5:
          score -= p3
          sal1[2] += p3

  t = abs(a3-a2)
  if t == 6:
      score -= p41
      sal0[3] += p41
  t = abs(a3-a1)
  if t == 6:
      score -= p42
      sal0[3] += p42
  t = abs(a1-a2)
  if t == 6:
    score -= p43
    sal0[3] += p43
  t = abs(b3-b2)
  if t == 6:
      score -= p41
      sal1[3] += p41
  t = abs(b3-b1)
  if t == 6:
      score -= p42
      sal1[3] += p42
  t = abs(b1-b2)
  if t == 6:
    score -= p43
    sal1[3] += p43


  if a3==1:
      if a1==4 or a2==4:
          score -= p5
          sal0[4] += p5
  if a3==2:
      if a1==11 or a2==11:
          score -= p5
          sal0[4] += p5
  if a3==3:
      if a1==6 or a2==6:
          score -= p5
          sal0[4] += p5
  if a3==4:
      if a1==1 or a2==1:
          score -= p5
          sal0[4] += p5
  if a3==6:
      if a1==9 or a2==9:
          score -= p5
          sal0[4] += p5
  if a3==8:
      if a1==11 or a2==11:
          score -= p5
          sal0[4] += p5
  if a3==9:
      if a1==6 or a2==6:
          score -= p5
          sal0[4] += p5
  if a3==11:
      if a1==8 or a2==8:
          score -= p5
          sal0[4] += p5
  if b3==1:
      if b1==4 or b2==4:
          score -= p5
          sal1[4] += p5
  if b3==2:
      if b1==11 or b2==11:
          score -= p5
          sal1[4] += p5
  if b3==3:
      if b1==6 or b2==6:
          score -= p5
          sal1[4] += p5
  if b3==4:
      if b1==1 or b2==1:
          score -= p5
          sal1[4] += p5
  if b3==6:
      if b1==9 or b2==9:
          score -= p5
          sal1[4] += p5
  if b3==8:
      if b1==11 or b2==11:
          score -= p5
          sal1[4] += p5
  if b3==9:
      if b1==6 or b2==6:
          score -= p5
          sal1[4] += p5
  if b3==11:
      if b1==8 or b2==8:
          score -= p5
          sal1[4] += p5

  if (a1==1 and a2==4) or (a1==2 and a2==11) or (a1==3 and a2==6) or (a1==6 and a2==9) or (a1==8 and a2==11):
      score -= p5
      sal0[4] += p5
  if (a1==4 and a2==1) or (a1==11 and a2==2) or (a1==6 and a2==3) or (a1==9 and a2==6) or (a1==11 and a2==8):
      score -= p5
      sal0[4] += p5
  if (b1==1 and b2==4) or (b1==2 and b2==11) or (b1==3 and b2==6) or (b1==6 and b2==9) or (b1==8 and b2==11):
      score -= p5
      sal1[4] += p5
  if (b1==4 and b2==1) or (b1==11 and b2==2) or (b1==6 and b2==3) or (b1==9 and b2==6) or (b1==11 and b2==8):
      score -= p5
      sal1[4] += p5

  if a3==7:
      if a1==4 or a2==4:
          score -= p6
          sal0[5] += p6
  if a3==5:
      if a1==2 or a2==2:
          score -= p6
          sal0[5] += p6
  if a3==4:
      if a1==7 or a2==7:
          score -= p6
          sal0[5] += p6
  if a3==2:
      if a1==5 or a2==5:
          score -= p6
          sal0[5] += p6
  if b3==7:
      if b1==4 or b2==4:
          score -= p6
          sal1[5] += p6
  if b3==5:
      if b1==2 or b2==2:
          score -= p6
          sal1[5] += p6
  if b3==4:
      if b1==7 or b2==7:
          score -= p6
          sal1[5] += p6
  if b3==2:
      if b1==5 or b2==5:
          score -= p6
          sal1[5] += p6
  if (a1==7 and a2==4) or (a1==5 and a2==2) or (a1==4 and a2==7) or (a1==2 and a2==5):
    score -= p6
    sal0[5] += p6
  if (b1==7 and b2==4) or (b1==5 and b2==2) or (b1==4 and b2==7) or (b1==2 and b2==5):
      score -= p6
      sal1[5] += p6


  if (token0[4]==5 and a3==5) or (token0[4]==4 and a3==2) or (token0[4]==3 and a3==11) or (token0[4]==2 and a3==8) or (token0[4]==1 and a3==5) or (token0[4]==10 and a3==2) or (token0[4]==9 and a3==11):
    if gender0==1:
      score -= p7
      sal0[6] += p7
    else:
      score -= p71
      sal0[6] += p71

  if (token1[4]==5 and b3==5) or (token1[4]==4 and b3==2) or (token1[4]==3 and b3==11) or (token1[4]==2 and b3==8) or (token1[4]==1 and b3==5) or (token1[4]==10 and b3==2) or (token1[4]==9 and b3==11):
    if gender1==1:
      score -= p7
      sal1[6] += p7
    else:
      score -= p71
      sal1[6] += p71

  if ( gender0 != 1 ) and ( ( token0[4] == 9 and a3 == 5 ) or ( token0[4] == 5 and a3 == 11 ) or ( token0[4] == 7 and a3 == 5 ) or ( token0[4] == 7 and a3 == 11 ) ):
    score -= p81
    sal0[7] += p81

  if ( gender0 !=1 ) and ( ( token0[2] == 9 and a2 == 5 ) or ( token0[2] == 5 and a2 == 11 ) or ( token0[2] == 7 and a2 == 5 ) or ( token0[4] == 7 and a2 == 11 )  ):
    score -= p82
    sal0[7] += p82

  if ( gender0 !=1 ) and ( ( token0[0] == 9 and a1 == 5 ) or ( token0[0] == 5 and a1 == 11 ) or ( token0[0] == 7 and a1 == 5 ) or ( token0[0] == 7 and a1 == 11 ) ) :
    score -= p83
    sal0[7] += p83

  if ( gender1 != 1 ) and ( ( token1[4] == 9 and b3 == 5 ) or ( token1[4] == 5 and b3 == 11 ) or ( token1[4] == 7 and b3 == 5 ) or ( token1[4] == 7 and b3 == 11 ) ) :
      score -= p81
      sal1[7] += p81

  if ( gender1 !=1 ) and ( ( token1[2] == 9 and b2 == 5 ) or ( token1[2] == 5 and b2 == 11 ) or ( token1[2] == 7 and b2 ==5 ) or ( token1[2] == 7 and b2 == 11 ) ) :
      score -= p82
      sal1[7] += p82

  if ( gender1 !=1 ) and ( ( token1[0] == 9 and b1 == 5 ) or ( token1[0] == 5 and b1 == 11 ) or ( token1[0] == 7 and b1 == 5 ) or ( token1[0] == 7 and b1 == 11 ) ) :
      score -= p83
      sal1[7] += p83

  return score, sal0, sal1


def getCalendar(year, month, day, hour, min):
  y = int(year)
  m = int(month)
  d = int(day)
  h = int(hour)
  minute = int(min)

  n1 = y*100 + m
  n2 = d*10000 + h*100 + minute
  data = np.loadtxt(calendarFile, delimiter=',', skiprows=1, encoding='euc-kr')

  b_y = data[y-1904][1]
  d_y = data[y-1904][3]
  f_y = data[y-1904][5]
  h_y = data[y-1904][7]
  j_y = data[y-1904][9]
  l_y = data[y-1904][11]
  n_y = data[y-1904][13]
  p_y = data[y-1904][15]
  r_y = data[y-1904][17]
  t_y = data[y-1904][19]
  v_y = data[y-1904][21]
  x_y = data[y-1904][23]
  c_y = data[y-1904][2]
  e_y = data[y-1904][4]
  g_y = data[y-1904][6]
  i_y = data[y-1904][8]
  k_y = data[y-1904][10]
  m_y = data[y-1904][12]
  o_y = data[y-1904][14]
  q_y = data[y-1904][16]
  s_y = data[y-1904][18]
  u_y = data[y-1904][20]
  w_y = data[y-1904][22]
  y_y = data[y-1904][24]

  mg=None

  ry = (y-1904)%10
  ys = ry+1
  if n1<d_y:
      ys -= 1
  if n1==d_y and n2<e_y:
      ys -= 1
  if ys == 0:
      ys=10

  ry2 = (y-1990)%12
  if ry2==0:
      yg=3
  if ry2==1:
      yg=4
  if ry2==2:
      yg=5
  if ry2==3:
      yg=6
  if ry2==4:
      yg=7
  if ry2==5:
      yg=8
  if ry2==6:
      yg=9
  if ry2==7:
      yg=10
  if ry2==8:
      yg=11
  if ry2==9:
      yg=12
  if ry2==10:
      yg=1
  if ry2==11:
      yg=2
  if n1<d_y:
      yg -= 1
  if n1==d_y and n2<e_y:
      yg -= 1
  if yg==0:
      yg=12

  if n1==b_y:
      if n2<c_y:
          mg=11
      else:
          mg=12
  if n1==d_y:
      if n2<e_y:
          mg=12
      else:
          mg=1
  if n1==f_y:
      if n2<g_y:
          mg=1
      else:
          mg=2
  if n1==h_y:
      if n2<i_y:
          mg=2
      else:
          mg=3
  if n1==j_y:
      if n2<k_y:
          mg=3
      else:
          mg=4
  if n1==l_y:
      if n2<m_y:
          mg=4
      else:
          mg=5
  if n1==n_y:
      if n2<o_y:
          mg=5
      else:
          mg=6
  if n1==p_y:
      if n2<q_y:
          mg=6
      else:
          mg=7
  if n1==r_y:
      if n2<s_y:
          mg=7
      else:
          mg=8
  if n1==t_y:
      if n2<u_y:
          mg=8
      else:
          mg=9
  if n1==v_y:
      if n2<w_y:
          mg=9
      else:
          mg=10
  if n1==x_y:
      if n2<y_y:
          mg=10
      else:
          mg=11

  if mg is None:
      mg = 1

  if ys==1 or ys==6:
      ms = 3+(mg-1)
  if ys==2 or ys==7:
      ms = 5+(mg-1)
  if ys==3 or ys==8:
      ms = 7+(mg-1)
  if ys==4 or ys==9:
      ms = 9+(mg-1)
  if ys==5 or ys==10:
      ms = 1+(mg-1)
  if ms>10:
      ms=ms-10

  yg = yg+2
  if yg==13:
      yg=1
  if yg==14:
      yg=2
  mg = mg+2
  if mg==13:
      mg=1
  if mg==14:
      mg=2

  return ys, yg, ms, mg


open_api_key = '568dd2436b1eacb12345eba0df5738e2db30c1860d124068fda6eb8520101285'

def get_birth():
  while(True):
    year =  input(" 태어난 연도 (birth year, 2000~2021) : ")
    t = int(year)
    if t>1999 and t<2022:
      break
    print("   Sorry, 연도를 다시 입력하세요 (2000 ~ 2021) : ")
  while(True):
    month = input(" 태어난 월 (month) : ")
    t = int(month)
    if t>0 and t<13:
      break
    print("   Sorry, 월을 다시 입력하세요 (1 ~ 12) : ")
  while(True):
    day0 =   input(" 태어난 일 (day) : ")
    t = int(day0)
    if t>0 and t<32:
      break;
    print("   Sorry, 날자를 확인하세요 (1~31) : ")

  if (len(month) <2):
    month = '0'+month
  if (len(day0) <2):
    day0 = '0'+day0
  day = day0

  params = '&solYear='+year+'&solMonth='+month+'&solDay='+day
  open_url = 'http://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getLunCalInfo?ServiceKey='+open_api_key+params
  response = requests.get(open_url)
  root = ET.fromstring(response.text)
  day0 = root[1][0][0][1].text
  ys,ye,ms,me = getCalendar(year,month,day, 12, 0)
  saju = []
  saju.append(ys)
  saju.append(ye)
  saju.append(ms)
  saju.append(me)
  saju.append(sky[day0[0]])
  saju.append(earth[day0[1]])
  return saju

def calculate_sky(i,j):
  t0 = np.eye(10)[np.array(i-1).reshape(-1)]
  t0 = t0.flatten()
  t1 = np.eye(10)[np.array(j-1).reshape(-1)]
  t1 = t1.flatten()
  s = np.concatenate((t0,t1))
  s = s.reshape(1,20)
  pred = model0.predict(s)
  return pred

def calculate_earth(i,j):
  t0 = np.eye(12)[np.array(i-1).reshape(-1)]
  t0 = t0.flatten()
  t1 = np.eye(12)[np.array(j-1).reshape(-1)]
  t1 = t1.flatten()
  s = np.concatenate((t0,t1))
  s = s.reshape(1,24)
  pred = model1.predict(s)
  return pred

def print_sal(sal0, sal1, gender0, gender1):
#  print("\n===================================")
  ss =["열정 에너지 예술 중독", "예민 직감 영적 불안", "감정기복 갈등 오해 고독", "강함 용감 충동 변화", "책임감 의리 완벽 자존심 인내", "충돌 자유 고집", "카리스마 승부욕 용감 외로움", "의지 솔직 직설 개성 고집 독립심"]
  sn = 0
  print("\n첫번째 사람의 성향 분석")
  for i, v in enumerate(sal0):
    if v>0:
      print(" ",ss[i])
      sn+=1
  if sn ==0:
    print(" 무난")
  sn = 0
  print("두번째 사람의 성향 분석")
  for j, v in enumerate(sal1):
    if v>0:
      print(" ",ss[j])
      sn+=1
  if sn==0:
    print(" 무난")


def main():
  while(True):
    print("\n===================================")
    print("첫번째 사람 ")
    gender0 = input(" 성별 :   남자 1 or 여자 (default) : ")
    if gender0=='1':
      gender0 = int(1)
    else:
      gender0 = int(0)
    person0 = get_birth()
    print("두번째 사람 ")
    gender1 = input(" 성별 :  남자 1 or 여자 (default) : ")
    if gender1=='1':
      gender1 = int(1)
    else:
      gender1 = int(0)
    person1 = get_birth()

    ys = calculate_sky(person0[0], person1[0])
    ms = calculate_sky(person0[2], person1[2])
    ds = calculate_sky(person0[4], person1[4])
    ye = calculate_earth(person0[1], person1[1])
    me = calculate_earth(person0[3], person1[3])
    de = calculate_earth(person0[5], person1[5])

    score = (0.6*ys) + (4.5*ds) + (1.0*ye) + (1.5*me) + (4.5*de)
    org_score = score.item()
    score, sal0, sal1 = calculate(person0, person1, gender0, gender1, score)
    print_sal(sal0, sal1, gender0, gender1)
    print("\n 매칭 점수 분석")
    print('   Original Score = ', org_score)
    print('   Final Score = ', score)
    if sum(sal0)>0 and sum(sal1)>0:
        stress = 0.5*(106-org_score)+(org_score-score)*1.8
    else:
        stress = 0.5*(106-org_score)+(org_score-score)
    print('   Stress Score = ', stress)

    if score<=35 and stress>=40:
        print('   위험  ')

if __name__ == "__main__":
  main()




def _get_open_api_key() -> str:
    """
    보조 함수: open_api_key 값을 가져옵니다.
    반환값: open_api_key 문자열
    """
    # 1) 환경 변수에서 가져오기
    env_key = os.getenv("SAJU_OPEN_API_KEY")
    if env_key:
        return env_key

    # 2) 원래 코드에서 가져오기
    try:
        from __main__ import open_api_key  # type: ignore[attr-defined]
        return open_api_key
    except ImportError:
        pass
    except NameError:
        pass

    try:
        # 글로벌 네임스페이스에서 직접 접근 시도
        return globals()["open_api_key"]
    except KeyError:
        pass

    raise RuntimeError(
        "open_api_key 값을 찾을 수 없습니다. "
        "환경 변수 'SAJU_OPEN_API_KEY' 를 설정하거나, "
    )


def get_birth_from_date(year: int, month: int, day: int):
    """
    주어진 양력 생년월일로부터 6개의 간지 토큰을 계산합니다.
    반환값: [년간, 년지, 월간, 월지, 일간, 일지] 의 리스트
        year: 태어난 연도 (예: 1990)
        month: 태어난 월 (1~12)
        day: 태어난 일 (1~31)
    """
    # 가정 - 아래 함수 및 변수들이 이미 정의되어 있음:
    #   - getCalendar(year, month, day, hour, minute)
    #   - sky / earth 딕셔너리
    api_key = _get_open_api_key()

    year_str = str(year)
    month_str = f"{month:02d}"
    day_str = f"{day:02d}"

    params = f"&solYear={year_str}&solMonth={month_str}&solDay={day_str}"
    open_url = (
        "http://apis.data.go.kr/B090041/openapi/service/"
        "LrsrCldInfoService/getLunCalInfo"
        f"?ServiceKey={api_key}{params}"
    )

    response = requests.get(open_url)
    response.raise_for_status()
    root = ET.fromstring(response.text)

    # 원래 코드와 동일하게 음력 일간/지 정보 추출
    day0 = root[1][0][0][1].text
    # 원래 코드와 동일하게 연간/지, 월간/지 계산
    ys, ye, ms, me = getCalendar(year_str, month_str, day_str, 12, 0)  # type: ignore[name-defined]

    saju = [
        ys,
        ye,
        ms,
        me,
        sky[day0[0]],     # type: ignore[name-defined]
        earth[day0[1]],   # type: ignore[name-defined]
    ]
    return saju


def run_match(
    year0: int,
    month0: int,
    day0: int,
    gender0: int,  # 1: 남자, 0: 여자
    year1: int,
    month1: int,
    day1: int,
    gender1: int,
):
    """
    두 사람의 생년월일과 성별을 받아 궁합 점수를 계산합니다.

    반환값: {
        "originalScore": float, # 원래 점수
        "finalScore": float,    # 최종 점수
        "stressScore": float,   # 스트레스 점수
        "sal0": List[float],    # 첫번째 사람의 살 리스트
        "sal1": List[float],    # 두번째 사람의 살 리스트
        "person0": List[int],   # 첫번째 사람의 6개 토큰
        "person1": List[int],   # 두번째 사람의 6개 토큰
        }
    """


    # 1) 각 사람의 6개 토큰 계산
    person0 = get_birth_from_date(year0, month0, day0)
    person1 = get_birth_from_date(year1, month1, day1)

    # 2) 원래 코드와 동일하게 하늘/땅 점수 계산
    ys = calculate_sky(person0[0], person1[0])  # type: ignore[name-defined]
    ms = calculate_sky(person0[2], person1[2])  # 최종 사용 안 함
    ds = calculate_sky(person0[4], person1[4])

    ye = calculate_earth(person0[1], person1[1])  # type: ignore[name-defined]
    me = calculate_earth(person0[3], person1[3])
    de = calculate_earth(person0[5], person1[5])

    # 3) 원래 코드와 동일하게 기본 점수 계산
    score = (0.6 * ys) + (4.5 * ds) + (1.0 * ye) + (1.5 * me) + (4.5 * de)
    org_score = float(score.item())

    # 4) 원래 코드와 동일하게 최종 점수 및 살 계산
    score, sal0, sal1 = calculate(   # type: ignore[name-defined]
        person0, person1, gender0, gender1, score
    )

    # float 변환
    final_score = float(score)

    # 5) 스트레스 점수 계산
    if sum(sal0) > 0 and sum(sal1) > 0:
        stress = 0.5 * (106 - org_score) + (org_score - final_score) * 1.8
    else:
        stress = 0.5 * (106 - org_score) + (org_score - final_score)

    stress_score = float(stress)

    # 6) 결과 반환
    result = {
        "originalScore": org_score,
        "finalScore": final_score,
        "stressScore": stress_score,
        "sal0": [float(x) for x in sal0],
        "sal1": [float(x) for x in sal1],
        "person0": [int(x) for x in person0],
        "person1": [int(x) for x in person1],
    }
    return result
