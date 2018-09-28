# -*- coding: utf-8 -*-
"""

@author: trusohamn
"""

import random           # FOR RANDOM BEGINNINGS
import tkinter   

"""working version of dividing cells"""

WIDTH = 600             # OF SCREEN IN PIXELS
HEIGHT = 600            # OF SCREEN IN PIXELS
pos_init = [WIDTH/6, HEIGHT/2]
dir_init = [1,0]
CELL_RADIUS = 10         # FOR ballS IN PIXELS
RING_WIDTH = CELL_RADIUS*2
FRAMES_PER_SEC = 3      # SCREEN UPDATE RATE
all_rings = set()
all_unfinished_branches = set()
life_span = 35
firstBranch = True
################################################################################

def main():
    # Start the program.
    initialise()
    tkinter.mainloop()

def initialise():
    # Setup simulation variables.
    new_ring = Ring(pos_init, dir_init)
    new_ring.color = 'white'
    new_ring.checked = True
    new_ring.finished = True
    all_rings.add(new_ring)
    Branch(new_ring)
    build_canvas()

def build_canvas():
    # Build GUI environment.
    global canvas
    root = tkinter.Tk()
    canvas = tkinter.Canvas(root, width=WIDTH, height=HEIGHT, background='black')
    canvas.after(int(1000 / FRAMES_PER_SEC), draw)
    canvas.pack()
   
def draw():
    #canvas.delete(ALL) 
    for c in all_rings:
        c.draw(canvas)  
        
    count = 0
    countall = 0
    while count < 3 or len(all_rings)<3: 
        ring = random.sample(all_rings, 1)[0]
        if ring.finished == True and ring.checked==False:
            count += 1
            ring.branch();
        countall += 1
        if countall>= len(all_rings):
            break
        
    tempBranches = set(all_unfinished_branches)    
    for branch in tempBranches:
        branch.addNewRing();
      
    # Main simulation loop.
    canvas.after(int(1000 / FRAMES_PER_SEC), draw)

    
def check_attachment(oldring, newring, turnmagenta):
    attachment = False
    for r in all_rings:
        if r != oldring and r!= newring:
            if (abs(r.position[0]- newring.position[0])<= 2*CELL_RADIUS and r.position[1] == newring.position[1]) or (abs(r.position[1]- newring.position[1])<= 2*CELL_RADIUS and r.position[0] == newring.position[0]):
                if turnmagenta:                
                    r.checked = True  
                    r.finished = True  
                    r.color = 'magenta'                
                attachment = True
                return attachment
    return attachment

def check_overlap(oldring, newring):
    overlap = False
    for r in all_rings:
        if  r!= newring and r != oldring:
            if (abs(r.position[0]- newring.position[0])<= CELL_RADIUS and r.position[1] == newring.position[1]) or (abs(r.position[1]- newring.position[1])<= CELL_RADIUS and r.position[0] == newring.position[0]):              
                overlap = True
                return overlap
    return overlap

################################################################################

# CELL IMPLEMENTATION CLASS

class Ring:

    def __init__(self, pos, direction, color = "gray"):
        self.position = pos
        self.direction = direction
        self.color = color
        self.size = CELL_RADIUS
        self.checked = False
        self.finished = False
        #print(pos[0], pos[1])
    
    def draw(self, canvas):
        x1 = self.position[0] - self.size
        y1 = self.position[1] - self.size
        x2 = self.position[0] + self.size
        y2 = self.position[1] + self.size
        canvas.create_rectangle((x1, y1, x2, y2), fill= self.color)        
    
    def finish(self):
        if self.finished==False:
            self.finished = True
            self.color = 'orange'
    
    def branch(self):
        if self.checked == False and self.finished :
            self.checked = True
            shouldBranch = random.choice([False, False, False, False, True])
            if shouldBranch:
                self.color = 'magenta'
                Branch(self)
            else:
                self.color = 'blue'
        
    def outOfBorders(self):
        if(self.position[0] - RING_WIDTH<=0 or self.position[0]+RING_WIDTH>=HEIGHT or self.position[1]-RING_WIDTH<=0 or self.position[1]+RING_WIDTH >=WIDTH ):
            return True
        else:
            return False
    
 
################################################################################
class Branch:
    def __init__(self, ringinit):
        global firstBranch
        self.rings = [ringinit]
        if(firstBranch):
            firstBranch = False
            self.size = 12
            self.dire = ringinit.direction
        else:
            self.size = random.randrange(7, 20)         
            dirx = 0 if ringinit.direction[0] != 0 else random.choice([-1,1])
            diry = 0 if ringinit.direction[1] != 0 else random.choice([-1,1])
            self.dire = [dirx,diry]
            #print(ringinit.direction, self.dire)
        all_unfinished_branches.add(self)
         
    def finished(self):
        all_unfinished_branches.remove(self)
        for ring in self.rings:
            ring.finish()
                 
    def addNewRing(self):
        prevRing = self.rings[-1]
        prevPos = prevRing.position
        newPos = [prevPos[0]+ self.dire[0]*CELL_RADIUS*2, prevPos[1]+self.dire[1]*CELL_RADIUS*2]
        newRing = Ring(newPos, self.dire)        
        if len(self.rings)>= self.size or newRing.outOfBorders():
            self.finished()
            prevRing.checked = True
            prevRing.color = 'cyan'
        elif check_overlap(prevRing, newRing):
            print('overlap')
            self.finished()
        elif len(self.rings)>1 and check_attachment(prevRing, newRing, True):
             self.rings.append(newRing)
             all_rings.add(newRing)
             self.finished()
        elif len(self.rings)==1 and check_attachment(prevRing, newRing, False):
             all_unfinished_branches.remove(self)
             prevRing.finished = True
             prevRing.checked = True
             prevRing.color = 'blue'
        
        else:
           self.rings.append(newRing)
           all_rings.add(newRing) 

            
 
################################################################################

# Execute the simulation.
if __name__ == '__main__':
    main()
