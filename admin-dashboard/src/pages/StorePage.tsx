import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { ShoppingBag, Plus, Trash2, Tag, Edit3, X } from 'lucide-react';

interface StoreItem {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
}

export function StorePage() {
  const queryClient = useQueryClient();
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: 0,
    imageUrl: ''
  });
  const [imageFile, setImageFile] = useState<File | null>(null);

  const { data: storeItems = [], isLoading } = useQuery<StoreItem[]>({
    queryKey: ['admin-store'],
    queryFn: () => api.get('/api/admin/store').then(r => r.data)
  });

  const addMutation = useMutation({
    mutationFn: (formDataObj: FormData) => api.post('/api/admin/store', formDataObj, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-store'] });
      setShowModal(false);
      setFormData({ name: '', description: '', price: 0, imageUrl: '' });
      setImageFile(null);
    }
  });

  const handleSubmit = () => {
    const data = new FormData();
    data.append('name', formData.name);
    data.append('description', formData.description);
    data.append('price', formData.price.toString());
    if (formData.imageUrl) data.append('imageUrl', formData.imageUrl);
    if (imageFile) data.append('image', imageFile);

    addMutation.mutate(data);
  };

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/api/admin/store/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-store'] })
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">E-Commerce Store</h1>
          <p className="text-sm text-[#718096] mt-1">Manage physical products and safety gear (Phase 6)</p>
        </div>
        <button 
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#00E5FF] text-[#0A0E1A] text-sm font-bold hover:bg-[#40EEFF] transition-colors"
        >
          <Plus className="w-4 h-4" />
          Add Product
        </button>
      </div>

      <div className="glass rounded-xl overflow-hidden">
        <div className="px-4 py-3 border-b border-white/5">
          <h2 className="text-sm font-semibold text-white">Product Catalog</h2>
        </div>
        
        {isLoading ? (
          <div className="p-4 text-center text-sm text-[#718096]">Loading store items...</div>
        ) : storeItems.length === 0 ? (
          <div className="p-8 text-center text-sm text-[#718096]">No products in the store yet.</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 p-4">
            {storeItems.map(item => (
              <div key={item.id} className="bg-[#1C2536] border border-white/5 rounded-xl overflow-hidden flex flex-col transition-transform hover:scale-[1.02]">
                <div className="h-40 bg-[#0F1628] w-full flex items-center justify-center relative overflow-hidden">
                   {item.imageUrl ? (
                     <img src={item.imageUrl} alt={item.name} className="w-full h-full object-cover" />
                   ) : (
                     <ShoppingBag className="w-12 h-12 text-[#2D3C56]" />
                   )}
                   <div className="absolute top-2 right-2 bg-[#00E5FF] text-[#0A0E1A] font-bold text-xs px-2 py-1 rounded-md shadow">
                     ${item.price.toFixed(2)}
                   </div>
                </div>
                <div className="p-4 flex flex-col flex-1">
                  <h3 className="text-white font-bold mb-1">{item.name}</h3>
                  <p className="text-sm text-[#718096] line-clamp-2 mb-4 flex-1">{item.description}</p>
                  
                  <div className="flex gap-2 mt-auto">
                    <button className="flex-1 py-1.5 rounded-lg bg-white/5 text-white text-xs font-medium hover:bg-white/10 transition-colors flex items-center justify-center gap-1">
                      <Edit3 className="w-3.5 h-3.5" />
                      Edit
                    </button>
                    <button 
                      onClick={() => deleteMutation.mutate(item.id)}
                      className="flex-1 py-1.5 rounded-lg bg-[#FF4D6D]/10 text-[#FF4D6D] text-xs font-medium hover:bg-[#FF4D6D]/20 transition-colors flex items-center justify-center gap-1"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-md bg-[#0F1628] border border-white/10 rounded-2xl p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-bold text-white">Add New Product</h3>
              <button onClick={() => setShowModal(false)} className="text-[#718096] hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Product Name</label>
                <input 
                  type="text" 
                  value={formData.name}
                  onChange={e => setFormData({ ...formData, name: e.target.value })}
                  className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF]"
                  placeholder="E.g., Child GPS Smartwatch"
                />
              </div>
              
              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Price ($)</label>
                <input 
                  type="number" 
                  value={formData.price || ''}
                  onChange={e => setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })}
                  className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF]"
                  placeholder="0.00"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Description</label>
                <textarea 
                  value={formData.description}
                  onChange={e => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF] h-20 resize-none"
                  placeholder="Detailed product description..."
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Image (File or URL)</label>
                <div className="flex gap-2">
                  <input 
                    type="file" 
                    accept="image/*"
                    onChange={e => setImageFile(e.target.files?.[0] || null)}
                    className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-1.5 text-white text-sm focus:outline-none focus:border-[#00E5FF] file:mr-4 file:py-1 file:px-4 file:rounded-full file:border-0 file:text-xs file:font-semibold file:bg-[#00E5FF]/10 file:text-[#00E5FF] hover:file:bg-[#00E5FF]/20"
                  />
                  <input 
                    type="text" 
                    value={formData.imageUrl}
                    onChange={e => setFormData({ ...formData, imageUrl: e.target.value })}
                    className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF]"
                    placeholder="Or enter URL"
                  />
                </div>
              </div>

              <button 
                onClick={handleSubmit}
                disabled={addMutation.isPending || !formData.name || formData.price <= 0 || (!formData.imageUrl && !imageFile)}
                className="w-full py-2.5 rounded-lg bg-[#00E5FF] text-[#0A0E1A] font-bold mt-4 hover:bg-[#40EEFF] disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                {addMutation.isPending ? 'Adding Product...' : 'Add Product'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
