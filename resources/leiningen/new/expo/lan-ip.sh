#!/bin/bash
echo `ip route get 8.8.8.8 | awk '{print $NF; exit}'` > .lan-ip
