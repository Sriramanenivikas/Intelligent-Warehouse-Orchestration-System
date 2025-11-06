-- =============================================
-- PINCODE DATABASE INITIALIZATION
-- =============================================
-- Indian Pincodes with Geolocation Data
-- Used for realistic load testing with 10K orders/sec
--
-- Coverage:
-- - Delhi NCR: 40+ pincodes
-- - Mumbai: 32+ pincodes
-- - Bangalore: 40+ pincodes
-- - Hyderabad: 32+ pincodes
-- - Chennai: 32+ pincodes
-- - Pune: 32+ pincodes
-- - Kolkata: 32+ pincodes
--
-- Total: 240+ pincodes for realistic distribution
-- =============================================

CREATE TABLE IF NOT EXISTS pincodes (
    pincode VARCHAR(6) PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    locality VARCHAR(200),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pincodes_city ON pincodes(city);
CREATE INDEX idx_pincodes_state ON pincodes(state);
CREATE INDEX idx_pincodes_location ON pincodes(latitude, longitude);

-- =============================================
-- DELHI NCR PINCODES
-- =============================================

INSERT INTO pincodes (pincode, city, state, locality, latitude, longitude) VALUES
-- Central Delhi
('110001', 'New Delhi', 'Delhi', 'Connaught Place', 28.6315, 77.2167),
('110002', 'New Delhi', 'Delhi', 'Daryaganj', 28.6500, 77.2400),
('110003', 'New Delhi', 'Delhi', 'Kamla Market', 28.6430, 77.2194),
('110005', 'New Delhi', 'Delhi', 'Karol Bagh', 28.6519, 77.1900),
('110006', 'New Delhi', 'Delhi', 'Ranjit Nagar', 28.6450, 77.1800),
('110007', 'New Delhi', 'Delhi', 'Moti Bagh', 28.5678, 77.1819),
('110008', 'New Delhi', 'Delhi', 'Patel Nagar', 28.6500, 77.1650),
('110009', 'New Delhi', 'Delhi', 'R K Puram', 28.5670, 77.1750),

-- South Delhi
('110010', 'New Delhi', 'Delhi', 'New Delhi Railway Station', 28.6431, 77.2197),
('110011', 'New Delhi', 'Delhi', 'India Gate', 28.6129, 77.2295),
('110012', 'New Delhi', 'Delhi', 'Lajpat Nagar', 28.5677, 77.2432),
('110016', 'New Delhi', 'Delhi', 'Nehru Place', 28.5494, 77.2501),
('110017', 'New Delhi', 'Delhi', 'Defence Colony', 28.5680, 77.2355),
('110018', 'New Delhi', 'Delhi', 'Saket', 28.5244, 77.2066),
('110019', 'New Delhi', 'Delhi', 'Greater Kailash', 28.5494, 77.2410),
('110020', 'New Delhi', 'Delhi', 'Munirka', 28.5556, 77.1714),

-- West Delhi
('110021', 'New Delhi', 'Delhi', 'Rajouri Garden', 28.6410, 77.1210),
('110022', 'New Delhi', 'Delhi', 'Janakpuri', 28.6219, 77.0814),
('110023', 'New Delhi', 'Delhi', 'Paschim Vihar', 28.6692, 77.1047),
('110027', 'New Delhi', 'Delhi', 'Rajouri Garden East', 28.6497, 77.1263),
('110029', 'New Delhi', 'Delhi', 'Ramesh Nagar', 28.6417, 77.1400),

-- North Delhi
('110030', 'New Delhi', 'Delhi', 'Kirti Nagar', 28.6556, 77.1489),
('110031', 'New Delhi', 'Delhi', 'Subzi Mandi', 28.6735, 77.2161),
('110032', 'New Delhi', 'Delhi', 'Bara Hindu Rao', 28.6764, 77.2156),
('110033', 'New Delhi', 'Delhi', 'Shakti Nagar', 28.6910, 77.2060),
('110034', 'New Delhi', 'Delhi', 'Tri Nagar', 28.7020, 77.1450),

-- East Delhi
('110035', 'New Delhi', 'Delhi', 'Wazirabad', 28.7100, 77.1780),
('110036', 'New Delhi', 'Delhi', 'Azadpur', 28.7145, 77.1765),
('110038', 'New Delhi', 'Delhi', 'Pulbangash', 28.7030, 77.2110),
('110039', 'New Delhi', 'Delhi', 'Azad Market', 28.6560, 77.2230),
('110040', 'New Delhi', 'Delhi', 'Vivek Vihar', 28.6719, 77.3150),

-- Noida
('110041', 'New Delhi', 'Delhi', 'Civil Lines', 28.6780, 77.2260),
('110042', 'New Delhi', 'Delhi', 'Delhi University', 28.6889, 77.2064),
('110044', 'New Delhi', 'Delhi', 'Kashmere Gate', 28.6675, 77.2278),
('110045', 'New Delhi', 'Delhi', 'Mayur Vihar Phase 1', 28.6089, 77.2974),
('110046', 'New Delhi', 'Delhi', 'Vasant Kunj', 28.5244, 77.1599),

-- =============================================
-- MUMBAI PINCODES
-- =============================================

-- South Mumbai
('400001', 'Mumbai', 'Maharashtra', 'Fort', 18.9322, 72.8264),
('400002', 'Mumbai', 'Maharashtra', 'Kalbadevi', 18.9478, 72.8323),
('400003', 'Mumbai', 'Maharashtra', 'Masjid Bunder', 18.9489, 72.8350),
('400004', 'Mumbai', 'Maharashtra', 'Girgaon', 18.9520, 72.8148),
('400005', 'Mumbai', 'Maharashtra', 'Colaba', 18.9220, 72.8264),
('400006', 'Mumbai', 'Maharashtra', 'Malabar Hill', 18.9538, 72.7984),
('400007', 'Mumbai', 'Maharashtra', 'Grant Road', 18.9636, 72.8153),
('400008', 'Mumbai', 'Maharashtra', 'Mumbai Central', 18.9694, 72.8195),

-- Central Mumbai
('400011', 'Mumbai', 'Maharashtra', 'Jacob Circle', 18.9804, 72.8267),
('400012', 'Mumbai', 'Maharashtra', 'Parel', 19.0063, 72.8397),
('400013', 'Mumbai', 'Maharashtra', 'Dadar', 19.0176, 72.8481),
('400014', 'Mumbai', 'Maharashtra', 'Dadar East', 19.0228, 72.8493),
('400015', 'Mumbai', 'Maharashtra', 'Sewri', 19.0040, 72.8557),
('400016', 'Mumbai', 'Maharashtra', 'Mahim', 19.0410, 72.8410),
('400017', 'Mumbai', 'Maharashtra', 'Dharavi', 19.0470, 72.8570),
('400018', 'Mumbai', 'Maharashtra', 'Worli', 19.0176, 72.8128),

-- Western Suburbs
('400049', 'Mumbai', 'Maharashtra', 'Andheri East', 19.1136, 72.8697),
('400050', 'Mumbai', 'Maharashtra', 'Bandra West', 19.0596, 72.8295),
('400051', 'Mumbai', 'Maharashtra', 'Bandra East', 19.0606, 72.8479),
('400052', 'Mumbai', 'Maharashtra', 'Bandra Railway Station', 19.0544, 72.8406),
('400053', 'Mumbai', 'Maharashtra', 'Andheri West', 19.1197, 72.8464),
('400054', 'Mumbai', 'Maharashtra', 'Vile Parle East', 19.0990, 72.8552),
('400055', 'Mumbai', 'Maharashtra', 'Parle', 19.1010, 72.8420),
('400056', 'Mumbai', 'Maharashtra', 'Vile Parle West', 19.1076, 72.8263),

-- Eastern Suburbs
('400059', 'Mumbai', 'Maharashtra', 'Goregaon East', 19.1653, 72.8804),
('400060', 'Mumbai', 'Maharashtra', 'Jogeshwari West', 19.1367, 72.8384),
('400061', 'Mumbai', 'Maharashtra', 'Malad West', 19.1869, 72.8481),
('400062', 'Mumbai', 'Maharashtra', 'Goregaon West', 19.1631, 72.8391),
('400063', 'Mumbai', 'Maharashtra', 'Malad East', 19.1881, 72.8489),
('400064', 'Mumbai', 'Maharashtra', 'Kandivali West', 19.2074, 72.8320),
('400065', 'Mumbai', 'Maharashtra', 'Borivali West', 19.2403, 72.8562),
('400066', 'Mumbai', 'Maharashtra', 'Borivali East', 19.2403, 72.8562),

-- =============================================
-- BANGALORE PINCODES
-- =============================================

-- Central Bangalore
('560001', 'Bangalore', 'Karnataka', 'MG Road', 12.9716, 77.5946),
('560002', 'Bangalore', 'Karnataka', 'Bangalore City', 12.9767, 77.5946),
('560003', 'Bangalore', 'Karnataka', 'Ulsoor', 12.9810, 77.6087),
('560004', 'Bangalore', 'Karnataka', 'Halasuru', 12.9716, 77.6213),
('560005', 'Bangalore', 'Karnataka', 'Domlur', 12.9591, 77.6387),
('560008', 'Bangalore', 'Karnataka', 'Shivaji Nagar', 12.9897, 77.6013),
('560009', 'Bangalore', 'Karnataka', 'Indiranagar', 12.9719, 77.6412),
('560010', 'Bangalore', 'Karnataka', 'Halasuru Market', 12.9835, 77.6039),

-- North Bangalore
('560011', 'Bangalore', 'Karnataka', 'Cantonment', 12.9916, 77.6005),
('560012', 'Bangalore', 'Karnataka', 'Palace Guttahalli', 13.0181, 77.5701),
('560013', 'Bangalore', 'Karnataka', 'Sadashivanagar', 13.0072, 77.5744),
('560016', 'Bangalore', 'Karnataka', 'Malleswaram', 13.0011, 77.5697),
('560017', 'Bangalore', 'Karnataka', 'Rajajinagar', 12.9899, 77.5553),
('560018', 'Bangalore', 'Karnataka', 'Seshadripuram', 12.9900, 77.5701),
('560019', 'Bangalore', 'Karnataka', 'Vijayanagar', 12.9716, 77.5336),
('560020', 'Bangalore', 'Karnataka', 'Basaveshwaranagar', 12.9810, 77.5359),

-- South Bangalore
('560021', 'Bangalore', 'Karnataka', 'Banashankari', 12.9250, 77.5463),
('560022', 'Bangalore', 'Karnataka', 'Girinagar', 12.9352, 77.5569),
('560023', 'Bangalore', 'Karnataka', 'Chikkalsandra', 12.9330, 77.5580),
('560024', 'Bangalore', 'Karnataka', 'Hanumanthnagar', 12.9450, 77.5705),
('560025', 'Bangalore', 'Karnataka', 'Jayanagar', 12.9250, 77.5838),
('560026', 'Bangalore', 'Karnataka', 'Richmond Town', 12.9685, 77.6030),
('560027', 'Bangalore', 'Karnataka', 'Shantinagar', 12.9716, 77.6006),
('560029', 'Bangalore', 'Karnataka', 'Chickpet', 12.9619, 77.5838),

-- East Bangalore
('560030', 'Bangalore', 'Karnataka', 'Chickpet', 12.9665, 77.5838),
('560032', 'Bangalore', 'Karnataka', 'Mahadevapura', 12.9899, 77.6972),
('560033', 'Bangalore', 'Karnataka', 'HAL', 12.9611, 77.6647),
('560034', 'Bangalore', 'Karnataka', 'Ulsoor Lake', 12.9820, 77.6200),
('560036', 'Bangalore', 'Karnataka', 'Whitefield', 12.9698, 77.7499),
('560037', 'Bangalore', 'Karnataka', 'Marathahalli', 12.9591, 77.7010),
('560038', 'Bangalore', 'Karnataka', 'Koramangala', 12.9279, 77.6271),
('560047', 'Bangalore', 'Karnataka', 'HSR Layout', 12.9116, 77.6473),

-- West Bangalore
('560040', 'Bangalore', 'Karnataka', 'Rajajinagar Industrial Area', 12.9899, 77.5301),
('560050', 'Bangalore', 'Karnataka', 'Rajajinagar Extension', 12.9899, 77.5446),
('560060', 'Bangalore', 'Karnataka', 'Nagapura', 12.9716, 77.5046),
('560070', 'Bangalore', 'Karnataka', 'Nagarbhavi', 12.9584, 77.5078),
('560079', 'Bangalore', 'Karnataka', 'Kengeri', 12.9081, 77.4858),
('560085', 'Bangalore', 'Karnataka', 'Yeshwanthpur', 13.0279, 77.5395),
('560092', 'Bangalore', 'Karnataka', 'Yelahanka', 13.1007, 77.5963),
('560094', 'Bangalore', 'Karnataka', 'Jakkur', 13.0782, 77.6008),

-- =============================================
-- HYDERABAD PINCODES
-- =============================================

-- Central Hyderabad
('500001', 'Hyderabad', 'Telangana', 'Abids', 17.3850, 78.4867),
('500002', 'Hyderabad', 'Telangana', 'Koti', 17.3754, 78.4809),
('500003', 'Hyderabad', 'Telangana', 'Kachiguda', 17.3850, 78.5025),
('500004', 'Hyderabad', 'Telangana', 'Sultan Bazar', 17.3894, 78.4776),
('500005', 'Hyderabad', 'Telangana', 'Nampally', 17.3961, 78.4656),
('500007', 'Hyderabad', 'Telangana', 'Himayatnagar', 17.4065, 78.4772),
('500008', 'Hyderabad', 'Telangana', 'Golconda Fort', 17.3833, 78.4011),
('500009', 'Hyderabad', 'Telangana', 'Goshamahal', 17.3850, 78.4600),

-- South Hyderabad
('500012', 'Hyderabad', 'Telangana', 'Santosh Nagar', 17.3376, 78.5025),
('500013', 'Hyderabad', 'Telangana', 'Malakpet', 17.3707, 78.5132),
('500015', 'Hyderabad', 'Telangana', 'Amberpet', 17.3972, 78.5389),
('500016', 'Hyderabad', 'Telangana', 'Himayat Nagar', 17.4019, 78.4804),
('500017', 'Hyderabad', 'Telangana', 'Narayanguda', 17.3976, 78.4963),
('500018', 'Hyderabad', 'Telangana', 'Champapet', 17.3587, 78.4710),
('500020', 'Hyderabad', 'Telangana', 'Banjara Hills', 17.4065, 78.4489),
('500022', 'Hyderabad', 'Telangana', 'Somajiguda', 17.4239, 78.4738),

-- North Hyderabad
('500024', 'Hyderabad', 'Telangana', 'Sikh Village', 17.4526, 78.5589),
('500025', 'Hyderabad', 'Telangana', 'Trimulgherry', 17.4839, 78.5076),
('500026', 'Hyderabad', 'Telangana', 'Secunderabad', 17.4399, 78.4983),
('500027', 'Hyderabad', 'Telangana', 'Marredpally', 17.4376, 78.5089),
('500028', 'Hyderabad', 'Telangana', 'Tarnaka', 17.4299, 78.5430),
('500029', 'Hyderabad', 'Telangana', 'Lothkunta', 17.4839, 78.5025),
('500030', 'Hyderabad', 'Telangana', 'Alwal', 17.5025, 78.5389),
('500031', 'Hyderabad', 'Telangana', 'Yapral', 17.5046, 78.5589),

-- West Hyderabad
('500032', 'Hyderabad', 'Telangana', 'Serilingampally', 17.4900, 78.3900),
('500033', 'Hyderabad', 'Telangana', 'Jubilee Hills', 17.4239, 78.4093),
('500034', 'Hyderabad', 'Telangana', 'Kukatpally', 17.4850, 78.4100),
('500035', 'Hyderabad', 'Telangana', 'Film Nagar', 17.4065, 78.4200),
('500036', 'Hyderabad', 'Telangana', 'Manikonda', 17.4100, 78.3800),
('500037', 'Hyderabad', 'Telangana', 'Gachibowli', 17.4399, 78.3489),
('500038', 'Hyderabad', 'Telangana', 'Madhapur', 17.4485, 78.3908),
('500039', 'Hyderabad', 'Telangana', 'Kondapur', 17.4650, 78.3635),

-- =============================================
-- CHENNAI PINCODES
-- =============================================

-- Central Chennai
('600001', 'Chennai', 'Tamil Nadu', 'Parrys', 13.0878, 80.2785),
('600002', 'Chennai', 'Tamil Nadu', 'Sowcarpet', 13.0899, 80.2892),
('600003', 'Chennai', 'Tamil Nadu', 'Park Town', 13.0824, 80.2894),
('600004', 'Chennai', 'Tamil Nadu', 'Mylapore', 13.0339, 80.2619),
('600005', 'Chennai', 'Tamil Nadu', 'Triplicane', 13.0569, 80.2778),
('600006', 'Chennai', 'Tamil Nadu', 'Chepauk', 13.0569, 80.2811),
('600007', 'Chennai', 'Tamil Nadu', 'Vepery', 13.0800, 80.2600),
('600008', 'Chennai', 'Tamil Nadu', 'Thousand Lights', 13.0569, 80.2600),

-- North Chennai
('600010', 'Chennai', 'Tamil Nadu', 'Perambur', 13.1143, 80.2378),
('600011', 'Chennai', 'Tamil Nadu', 'Kilpauk', 13.0800, 80.2378),
('600012', 'Chennai', 'Tamil Nadu', 'Perambur Barracks', 13.1100, 80.2400),
('600013', 'Chennai', 'Tamil Nadu', 'Tondiarpet', 13.1264, 80.2778),
('600014', 'Chennai', 'Tamil Nadu', 'Washermanpet', 13.1100, 80.2900),
('600015', 'Chennai', 'Tamil Nadu', 'Kodungaiyur', 13.1350, 80.2500),
('600016', 'Chennai', 'Tamil Nadu', 'Purasawalkam', 13.0900, 80.2500),
('600017', 'Chennai', 'Tamil Nadu', 'T Nagar', 13.0418, 80.2341),

-- South Chennai
('600018', 'Chennai', 'Tamil Nadu', 'Kodambakkam', 13.0500, 80.2250),
('600020', 'Chennai', 'Tamil Nadu', 'Anna Nagar', 13.0850, 80.2101),
('600021', 'Chennai', 'Tamil Nadu', 'Nandanam', 13.0339, 80.2410),
('600024', 'Chennai', 'Tamil Nadu', 'Saidapet', 13.0215, 80.2231),
('600025', 'Chennai', 'Tamil Nadu', 'Ashok Nagar', 13.0339, 80.2101),
('600026', 'Chennai', 'Tamil Nadu', 'Adyar', 13.0067, 80.2570),
('600028', 'Chennai', 'Tamil Nadu', 'Besant Nagar', 13.0013, 80.2669),
('600029', 'Chennai', 'Tamil Nadu', 'Mandaveli', 13.0280, 80.2600),

-- West Chennai
('600030', 'Chennai', 'Tamil Nadu', 'Guindy', 13.0067, 80.2206),
('600031', 'Chennai', 'Tamil Nadu', 'Ekkatuthangal', 13.0215, 80.2025),
('600032', 'Chennai', 'Tamil Nadu', 'Porur', 13.0339, 80.1570),
('600033', 'Chennai', 'Tamil Nadu', 'Valasaravakkam', 13.0418, 80.1689),
('600034', 'Chennai', 'Tamil Nadu', 'Virugambakkam', 13.0569, 80.2025),
('600035', 'Chennai', 'Tamil Nadu', 'K K Nagar', 13.0418, 80.2025),
('600036', 'Chennai', 'Tamil Nadu', 'Vadapalani', 13.0500, 80.2101),
('600037', 'Chennai', 'Tamil Nadu', 'Anna Nagar West', 13.0850, 80.1950),

-- =============================================
-- PUNE PINCODES
-- =============================================

-- Central Pune
('411001', 'Pune', 'Maharashtra', 'Kasba Peth', 18.5204, 73.8567),
('411002', 'Pune', 'Maharashtra', 'Sadashiv Peth', 18.5132, 73.8489),
('411003', 'Pune', 'Maharashtra', 'Narayan Peth', 18.5132, 73.8600),
('411004', 'Pune', 'Maharashtra', 'Shivajinagar', 18.5304, 73.8489),
('411005', 'Pune', 'Maharashtra', 'Ganj Peth', 18.5204, 73.8700),
('411006', 'Pune', 'Maharashtra', 'Shukrawar Peth', 18.5132, 73.8567),
('411007', 'Pune', 'Maharashtra', 'Pune Railway Station', 18.5284, 73.8740),
('411008', 'Pune', 'Maharashtra', 'Shaniwar Peth', 18.5176, 73.8600),

-- West Pune
('411009', 'Pune', 'Maharashtra', 'Deccan Gymkhana', 18.5176, 73.8420),
('411011', 'Pune', 'Maharashtra', 'Shivaji Nagar', 18.5304, 73.8489),
('411012', 'Pune', 'Maharashtra', 'Erandwane', 18.5100, 73.8350),
('411013', 'Pune', 'Maharashtra', 'Parvati', 18.4938, 73.8567),
('411014', 'Pune', 'Maharashtra', 'Salisbury Park', 18.5284, 73.8350),
('411015', 'Pune', 'Maharashtra', 'Shivaji Nagar', 18.5284, 73.8489),
('411016', 'Pune', 'Maharashtra', 'Swargate', 18.5018, 73.8636),
('411017', 'Pune', 'Maharashtra', 'Bibvewadi', 18.4700, 73.8567),

-- North Pune
('411018', 'Pune', 'Maharashtra', 'Aundh', 18.5579, 73.8067),
('411019', 'Pune', 'Maharashtra', 'Pimpri', 18.6298, 73.7997),
('411020', 'Pune', 'Maharashtra', 'Bopodi', 18.5579, 73.8489),
('411021', 'Pune', 'Maharashtra', 'Khadki', 18.5646, 73.8420),
('411022', 'Pune', 'Maharashtra', 'Range Hills', 18.5579, 73.8600),
('411023', 'Pune', 'Maharashtra', 'Sangvi', 18.5579, 73.7997),
('411025', 'Pune', 'Maharashtra', 'Koregaon Park', 18.5418, 73.8936),
('411026', 'Pune', 'Maharashtra', 'Viman Nagar', 18.5679, 73.9143),

-- East Pune
('411027', 'Pune', 'Maharashtra', 'Hadapsar', 18.5089, 73.9260),
('411028', 'Pune', 'Maharashtra', 'Pune Cantonment', 18.5204, 73.8936),
('411029', 'Pune', 'Maharashtra', 'Lohegaon', 18.5889, 73.9260),
('411030', 'Pune', 'Maharashtra', 'Wagholi', 18.5810, 73.9810),
('411031', 'Pune', 'Maharashtra', 'Kharadi', 18.5510, 73.9470),
('411032', 'Pune', 'Maharashtra', 'Mundhwa', 18.5304, 73.9323),
('411033', 'Pune', 'Maharashtra', 'Kondhwa', 18.4582, 73.8936),
('411034', 'Pune', 'Maharashtra', 'NIBM', 18.4700, 73.8850),

-- =============================================
-- KOLKATA PINCODES
-- =============================================

-- Central Kolkata
('700001', 'Kolkata', 'West Bengal', 'Dalhousie', 22.5726, 88.3639),
('700002', 'Kolkata', 'West Bengal', 'Hare Street', 22.5726, 88.3700),
('700003', 'Kolkata', 'West Bengal', 'Bowbazar', 22.5726, 88.3550),
('700004', 'Kolkata', 'West Bengal', 'Barabazar', 22.5726, 88.3550),
('700005', 'Kolkata', 'West Bengal', 'Alipore', 22.5326, 88.3300),
('700006', 'Kolkata', 'West Bengal', 'Chowringhee', 22.5478, 88.3550),
('700007', 'Kolkata', 'West Bengal', 'Dharmatala', 22.5549, 88.3550),
('700008', 'Kolkata', 'West Bengal', 'China Town', 22.5610, 88.3639),

-- South Kolkata
('700009', 'Kolkata', 'West Bengal', 'Bhowanipore', 22.5270, 88.3480),
('700010', 'Kolkata', 'West Bengal', 'New Alipore', 22.5220, 88.3350),
('700012', 'Kolkata', 'West Bengal', 'Ballygunge', 22.5326, 88.3639),
('700013', 'Kolkata', 'West Bengal', 'Tollygunge', 22.4970, 88.3550),
('700014', 'Kolkata', 'West Bengal', 'Lake Gardens', 22.5200, 88.3500),
('700015', 'Kolkata', 'West Bengal', 'Jodhpur Park', 22.4970, 88.3600),
('700016', 'Kolkata', 'West Bengal', 'Gariahat', 22.5176, 88.3639),
('700017', 'Kolkata', 'West Bengal', 'Lake Market', 22.5220, 88.3590),

-- North Kolkata
('700025', 'Kolkata', 'West Bengal', 'Shyambazar', 22.6010, 88.3750),
('700026', 'Kolkata', 'West Bengal', 'Belgachia', 22.6010, 88.3600),
('700027', 'Kolkata', 'West Bengal', 'Chitpur', 22.6010, 88.3700),
('700028', 'Kolkata', 'West Bengal', 'Cossipore', 22.6200, 88.3750),
('700029', 'Kolkata', 'West Bengal', 'Dum Dum', 22.6500, 88.4250),
('700030', 'Kolkata', 'West Bengal', 'Baranagar', 22.6450, 88.3700),
('700031', 'Kolkata', 'West Bengal', 'Belgharia', 22.6750, 88.3900),
('700032', 'Kolkata', 'West Bengal', 'Lake Town', 22.5750, 88.4050),

-- East Kolkata
('700034', 'Kolkata', 'West Bengal', 'Tangra', 22.5580, 88.3900),
('700035', 'Kolkata', 'West Bengal', 'Topsia', 22.5410, 88.3900),
('700036', 'Kolkata', 'West Bengal', 'Park Circus', 22.5410, 88.3700),
('700037', 'Kolkata', 'West Bengal', 'Entally', 22.5650, 88.3700),
('700038', 'Kolkata', 'West Bengal', 'Kasba', 22.5200, 88.3850),
('700039', 'Kolkata', 'West Bengal', 'Jadavpur', 22.4970, 88.3800),
('700040', 'Kolkata', 'West Bengal', 'Santoshpur', 22.5050, 88.3900),
('700041', 'Kolkata', 'West Bengal', 'Garia', 22.4650, 88.3900);

-- =============================================
-- STATISTICS
-- =============================================

-- Total pincodes by city
SELECT
    city,
    COUNT(*) as pincode_count,
    MIN(latitude) as min_lat,
    MAX(latitude) as max_lat,
    MIN(longitude) as min_lon,
    MAX(longitude) as max_lon
FROM pincodes
GROUP BY city
ORDER BY pincode_count DESC;

-- Verify data loaded
SELECT
    COUNT(*) as total_pincodes,
    COUNT(DISTINCT city) as unique_cities,
    COUNT(DISTINCT state) as unique_states
FROM pincodes;

COMMIT;
