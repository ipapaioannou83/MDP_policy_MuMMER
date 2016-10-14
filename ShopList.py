# -*- coding: utf-8 -*-
"""
Created on Tue Oct 11 13:49:48 2016

@author: elveleg
"""

import numpy as np
from Shop import Shop

class ShopList(list):
    
    def __init__(self):        
        data = np.recfromcsv("/media/elveleg/Data/MuMMER Project/MuMMER_MDP/shop_list.txt", delimiter=';')
        
        for s in data:
            shop = Shop(s[0], s[1], s[3])
            self.append(shop)
    
    def filteredCategory(self, cat):
        list = []
        for s in self:
            if s.getCategory() == cat:
                list.append(s)
        return list
        
    def getShop(self, shopName):
        for s in self:
            if s.getName() == shopName:
                return s