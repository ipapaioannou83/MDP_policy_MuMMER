# -*- coding: utf-8 -*-
"""
Created on Tue Oct 11 13:49:48 2016

@author: elveleg
"""

import numpy as np
from Shop import Shop

class ShopList(list):
    
    def __init__(self, empty = False):   
        if empty == False:
            data = np.recfromcsv("/media/elveleg/Data/MuMMER Project/MuMMER_MDP/shop_list.txt", delimiter=';')
            #data = np.recfromcsv("D:\MuMMER Project\MuMMER_MDP\shop_list.txt", delimiter=';')
    
            for s in data:
                shop = Shop(s[0], s[1], s[3])
                self.append(shop)
                
    
    def filteredCategory(self, cat):
        new = ShopList(True)
        for s in self:
            if s.getCategory() == cat:
                new.append(s)
        return new
        
#    def filteredCategory(self, cat):
#        for s in self:
#            if s.getCategory() != cat:
#                self.remove(s)
#        return self
        
    def getShop(self, shopName):
        for s in self:
            if s.getName() == shopName:
                return s
    
    def enumShops(self):
        res = ""
        for i in self[:-1]:
            res += " " + i.getName() + ", "
            
        for i in self[-1:]:        
            res += " and " + i.getName()
        return res
        
    def getDirections(self, shopName):
         for s in self:
            if s.getName() == shopName:
                return s.getDirections()