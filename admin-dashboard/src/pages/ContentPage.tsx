import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { FileText, Video, MessageSquare, Plus, Trash2, Eye, X } from 'lucide-react';

interface ContentItem {
  id: string;
  type: string;
  title: string;
  publishedAt: string;
}

export function ContentPage() {
  const queryClient = useQueryClient();
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    type: 'POST',
    title: '',
    description: '',
    url: '',
    thumbnailUrl: '',
    category: 'GENERAL'
  });
  const [mediaFile, setMediaFile] = useState<File | null>(null);
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);

  const { data: contentList = [], isLoading } = useQuery<ContentItem[]>({
    queryKey: ['admin-content'],
    queryFn: () => api.get('/api/admin/content').then(r => r.data)
  });

  const publishMutation = useMutation({
    mutationFn: (formDataObj: FormData) => api.post('/api/admin/content', formDataObj, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-content'] });
      setShowModal(false);
      setFormData({ type: 'POST', title: '', description: '', url: '', thumbnailUrl: '', category: 'GENERAL' });
      setMediaFile(null);
      setThumbnailFile(null);
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/api/admin/content/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-content'] })
  });

  const handleSubmit = () => {
    const data = new FormData();
    data.append('type', formData.type);
    data.append('title', formData.title);
    data.append('description', formData.description);
    data.append('category', formData.category);
    if (formData.url) data.append('url', formData.url);
    if (formData.thumbnailUrl) data.append('thumbnailUrl', formData.thumbnailUrl);
    
    if (mediaFile) data.append('media', mediaFile);
    if (thumbnailFile) data.append('thumbnail', thumbnailFile);

    publishMutation.mutate(data);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Educational Content</h1>
          <p className="text-sm text-[#718096] mt-1">Admin-curated content visible to families (Phase 6)</p>
        </div>
        <button 
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#00E5FF] text-[#0A0E1A] text-sm font-bold hover:bg-[#40EEFF] transition-colors"
        >
          <Plus className="w-4 h-4" />
          Publish Content
        </button>
      </div>

      <div className="glass rounded-xl overflow-hidden">
        <div className="px-4 py-3 border-b border-white/5">
          <h2 className="text-sm font-semibold text-white">Published Content</h2>
        </div>
        {isLoading ? (
          <div className="p-4 text-center text-sm text-[#718096]">Loading content...</div>
        ) : contentList.length === 0 ? (
          <div className="p-8 text-center text-sm text-[#718096]">No content published yet.</div>
        ) : contentList.map(item => (
          <div key={item.id} className="flex items-center gap-4 px-4 py-4 border-b border-white/5 last:border-0 hover:bg-white/2 transition-colors">
            <div className="w-8 h-8 rounded-lg bg-[#00E5FF]/10 flex items-center justify-center flex-shrink-0">
              {item.type === 'VIDEO' || item.type === 'REEL' ? (
                <Video className="w-4 h-4 text-[#00E5FF]" />
              ) : item.type === 'POST' ? (
                <FileText className="w-4 h-4 text-[#6C63FF]" />
              ) : (
                <MessageSquare className="w-4 h-4 text-[#FFB347]" />
              )}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm text-white font-medium truncate">{item.title}</p>
              <p className="text-xs text-[#718096]">{item.type} · Published {new Date(item.publishedAt).toLocaleDateString()}</p>
            </div>
            <span className="px-2 py-0.5 rounded-full text-xs bg-[#00D68F]/10 text-[#00D68F]">Live</span>
            <div className="flex gap-1">
              <button 
                onClick={() => deleteMutation.mutate(item.id)}
                className="p-1.5 rounded-lg hover:bg-[#FF4D6D]/10 text-[#718096] hover:text-[#FF4D6D] transition-colors"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        ))}
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
          <div className="w-full max-w-md bg-[#0F1628] border border-white/10 rounded-2xl p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-bold text-white">Publish New Content</h3>
              <button onClick={() => setShowModal(false)} className="text-[#718096] hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Content Type</label>
                <select 
                  value={formData.type}
                  onChange={e => setFormData({ ...formData, type: e.target.value })}
                  className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF]"
                >
                  <option value="POST">Article / Post</option>
                  <option value="VIDEO">Long-form Video</option>
                  <option value="REEL">Short Reel</option>
                  <option value="STATUS">24h Status Update</option>
                </select>
              </div>
              
              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Title</label>
                <input 
                  type="text" 
                  value={formData.title}
                  onChange={e => setFormData({ ...formData, title: e.target.value })}
                  className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF]"
                  placeholder="E.g., How to stay safe online"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Description</label>
                <textarea 
                  value={formData.description}
                  onChange={e => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF] h-20 resize-none"
                  placeholder="Short description..."
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Media (File or URL)</label>
                <div className="flex gap-2">
                  <input 
                    type="file" 
                    onChange={e => setMediaFile(e.target.files?.[0] || null)}
                    className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-1.5 text-white text-sm focus:outline-none focus:border-[#00E5FF] file:mr-4 file:py-1 file:px-4 file:rounded-full file:border-0 file:text-xs file:font-semibold file:bg-[#00E5FF]/10 file:text-[#00E5FF] hover:file:bg-[#00E5FF]/20"
                  />
                  <input 
                    type="text" 
                    value={formData.url}
                    onChange={e => setFormData({ ...formData, url: e.target.value })}
                    className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF]"
                    placeholder="Or enter URL"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-xs font-medium text-[#718096] mb-1">Thumbnail (File or URL)</label>
                <div className="flex gap-2">
                  <input 
                    type="file" 
                    accept="image/*"
                    onChange={e => setThumbnailFile(e.target.files?.[0] || null)}
                    className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-1.5 text-white text-sm focus:outline-none focus:border-[#00E5FF] file:mr-4 file:py-1 file:px-4 file:rounded-full file:border-0 file:text-xs file:font-semibold file:bg-[#00E5FF]/10 file:text-[#00E5FF] hover:file:bg-[#00E5FF]/20"
                  />
                  <input 
                    type="text" 
                    value={formData.thumbnailUrl}
                    onChange={e => setFormData({ ...formData, thumbnailUrl: e.target.value })}
                    className="w-full bg-[#1C2536] border border-white/5 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:border-[#00E5FF]"
                    placeholder="Or enter URL"
                  />
                </div>
              </div>

              <button 
                onClick={handleSubmit}
                disabled={publishMutation.isPending || !formData.title || (!formData.url && !mediaFile)}
                className="w-full py-2.5 rounded-lg bg-[#00E5FF] text-[#0A0E1A] font-bold mt-4 hover:bg-[#40EEFF] disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                {publishMutation.isPending ? 'Publishing...' : 'Publish Now'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
